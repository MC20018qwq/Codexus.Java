package codexus.cipher.protocol;

import codexus.cipher.entities.com4399.*;
import codexus.cipher.captcha.LocalCaptchaRecognizer;
import codexus.cipher.extensions.com4399extensions.CaptchaHandler;
import codexus.cipher.utils.http.HttpWrapper;
import codexus.cipher.utils.http.ParameterBuilder;
import codexus.csharpsup.DotNetJson;
import codexus.csharpsup.DotNetUri;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/** 4399 OAuth account login, including device persistence and interactive verification. */
public final class Com4399 implements AutoCloseable {
    private static final String USER_AGENT = "Mozilla/5.0 (Linux; Android 14; PFJM10 Build/UKQ1.230924.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/120.0.6099.230 Mobile Safari/537.36";
    private static final Pattern CAPTCHA_ID = Pattern.compile(
            "name\\s*=\\s*[\"']captcha_id[\"']\\s+value\\s*=\\s*[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
    private final HttpWrapper api, login, authorization;
    private final MgbSdk sdk;
    private final CaptchaHandler captchaHandler;
    private final LocalCaptchaRecognizer captchaRecognizer = new LocalCaptchaRecognizer();
    private final Path deviceFile;
    private final Object deviceLock = new Object();
    private Entity4399Device device;

    public Com4399() {
        this(new HttpWrapper("https://m.4399api.com", Com4399::configureHeaders), createLoginClient(),
                new HttpWrapper("", Com4399::configureHeaders),
                new MgbSdk("x19"), new CaptchaHandler(), Path.of("4399com.cds"));
    }

    /** Supplied login transport must expose redirects (HttpClient.Redirect.NEVER). */
    public Com4399(HttpWrapper api, HttpWrapper login, HttpWrapper authorization, MgbSdk sdk,
                   CaptchaHandler captchaHandler, Path deviceFile) {
        this.api = Objects.requireNonNull(api);
        this.login = Objects.requireNonNull(login);
        this.authorization = Objects.requireNonNull(authorization);
        this.sdk = Objects.requireNonNull(sdk);
        this.captchaHandler = Objects.requireNonNull(captchaHandler);
        this.deviceFile = Objects.requireNonNull(deviceFile);
        if (login.getClient().followRedirects() != HttpClient.Redirect.NEVER)
            throw new IllegalArgumentException("4399 OAuth login requires a transport with redirects disabled");
        try { createOrLoadDeviceAsync().join(); }
        catch (RuntimeException e) { close(); throw e; }
    }

    private static void configureHeaders(HttpWrapper.HttpWrapperBuilder builder) {
        builder.userAgent(USER_AGENT).addHeader("X-Requested-With", "com.netease.mc.m4399");
    }

    private static HttpWrapper createLoginClient() {
        var client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NEVER)
                .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL)).build();
        return new HttpWrapper("https://ptlogin.4399.com", builder -> {
            configureHeaders(builder);
            builder.addHeader("Referer", "https://ptlogin.4399.com/oauth2/authorize.do");
        }, client, null);
    }

    private CompletableFuture<Void> createOrLoadDeviceAsync() {
        try {
            device = DotNetJson.deserialize(Files.readString(deviceFile, StandardCharsets.UTF_8), Entity4399Device.class);
            if (device != null && device.DeviceState != null) return CompletableFuture.completedFuture(null);
        } catch (NoSuchFileException ignored) {
            // An absent cache requires device OAuth, not account registration.
        } catch (IOException e) { return CompletableFuture.failedFuture(new UncheckedIOException(e)); }
        device = new Entity4399Device();
        device.DeviceIdentifier = generateIdentifier();
        device.DeviceIdentifierSm = generateIdentifier();
        device.DeviceUdid = UUID.randomUUID().toString();
        return oauthDevice().thenAccept(state -> { device.DeviceState = state; saveDevice(); });
    }

    private CompletableFuture<String> oauthDevice() {
        var oauth = new Entity4399OAuth();
        oauth.DeviceIdentifier = device.DeviceIdentifier;
        oauth.DeviceIdentifierSm = device.DeviceIdentifierSm;
        oauth.Udid = device.DeviceUdid;
        String body = new ParameterBuilder().append("usernames", "").append("top_bar", "1").append("state", "")
                .append("device", DotNetJson.serializeRelaxed(oauth)).formUrlEncode();
        return api.postAsync("/openapiv2/oauth.html", body, "application/x-www-form-urlencoded").thenApply(response -> {
            HttpWrapper.ensureSuccess(response);
            var result = DotNetJson.deserialize(HttpWrapper.readAsString(response), Entity4399OAuthResponse.class);
            if (result == null || result.Result == null) throw new IllegalStateException("Invalid 4399 device OAuth response");
            String state = new ParameterBuilder(result.Result.LoginUrl).get("state");
            if (state.isEmpty()) throw new IllegalStateException("4399 device OAuth did not return state");
            return state;
        });
    }

    public CompletableFuture<String> loginAndAuthorize(String username, String password) {
        return loginAndAuthorize(username, password, null, null, 0);
    }
    public CompletableFuture<String> loginAndAuthorize(String username, String password,
                                                       String captcha, String captchaId, int retry) {
        if (retry > 5) return CompletableFuture.failedFuture(new IllegalStateException("Retry limit exceeded"));
        return requestStateAsync().thenCompose(state -> {
            var parameters = new ParameterBuilder().append("isInputRealname", "false").append("isValidRealname", "false")
                    .append("sec", "1").append("password", password).append("username", username.toLowerCase(Locale.ROOT))
                    .append("css", "").append("show_close_button", "").append("response_type", "TOKEN")
                    .append("client_id", "40f9e9b95d6c71ba5c6e0bd14c0abeff").append("show_4399", "")
                    .append("username_history", "").append("uid", "").append("expand_ext_login_list", "")
                    .append("ref", "{\"game\":\"115716\",\"channel\":\"\"}").append("autoCreateAccount", "")
                    .append("scope", "basic").append("bizId", "2100001792").append("state", state)
                    .append("show_ext_login", "").append("reg_mode", "reg_phone").append("_d", device.DeviceIdentifier)
                    .append("show_back_button", "").append("auto_scroll", "").append("access_token", "")
                    .append("show_forget_password", "").append("auth_action", "ORILOGIN")
                    .append("redirect_uri", "https://m.4399api.com/openapi/oauth-callback.html?gamekey=44770&game_key=115716")
                    .append("show_topbar", "false").append("aid", "").append("cid", "");
            if (captcha != null && captchaId != null) parameters.append("captcha", captcha).append("captcha_id", captchaId);
            return login.postAsync("/oauth2/loginAndAuthorize.do?channel=&sdk=op&sdk_version=3.12.2.503",
                    parameters.formUrlEncode(), "application/x-www-form-urlencoded");
        }).thenCompose(response -> {
            String text = HttpWrapper.readAsString(response);
            if (text.contains("验证码")) return handleCaptchaWithHtml(username, password, text, retry);
            String location = response.headers().firstValue("Location").orElse(null);
            if (location == null) {
                if (text.contains("用户名或密码错误") || text.contains("密码错误"))
                    throw new IllegalStateException("用户名或密码错误");
                throw new IllegalStateException("4399 login did not return an authorization redirect (HTTP " + response.statusCode() + ")");
            }
            URI redirect = response.uri().resolve(location);
            if (captcha != null && captchaId == null) {
                // Keep escaped OAuth parameters intact; only encode the new captcha value.
                String raw = redirect.toASCIIString();
                int fragmentIndex = raw.indexOf('#');
                String fragment = fragmentIndex < 0 ? "" : raw.substring(fragmentIndex);
                if (fragmentIndex >= 0) raw = raw.substring(0, fragmentIndex);
                redirect = URI.create(raw + (redirect.getRawQuery() == null ? "?" : "&")
                        + "captcha=" + DotNetUri.escapeDataString(captcha) + fragment);
            }
            return authorization.getAsync(redirect.toASCIIString()).thenCompose(authorized -> {
                HttpWrapper.ensureSuccess(authorized);
                String resultJson = HttpWrapper.readAsString(authorized);
                if (resultJson.contains("登录状态已失效，请重新登录"))
                    return oauthDevice().thenCompose(state -> {
                        device.DeviceState = state; saveDevice();
                        return loginAndAuthorize(username, password, null, null, retry + 1);
                    });
                var root = DotNetJson.parse(resultJson);
                if ("103".equals(root.path("code").asText()))
                    return captchaHandler.handleLoginCaptchaAsync(resultJson)
                            .thenCompose(answer -> loginAndAuthorize(username, password, answer, null, retry + 1));
                var user = DotNetJson.deserialize(resultJson, Entity4399UserInfoResponse.class);
                if (user == null || !"100".equals(user.Code) || user.Result == null || user.Result.State.isEmpty())
                    throw new IllegalStateException("4399 did not return an authorized account session");
                return CompletableFuture.completedFuture(sdk.generateSAuth(UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT),
                        "", Long.toString(user.Result.Uid), user.Result.State, "", "4399com", "ad"));
            });
        });
    }

    private CompletableFuture<String> requestStateAsync() {
        return api.getAsync("/openapi/oauth-callback.html?gamekey=44770&game_key=115716").thenApply(response -> {
            HttpWrapper.ensureSuccess(response);
            var callback = DotNetJson.deserialize(HttpWrapper.readAsString(response), Entity4399OAuthCallback.class);
            if (callback == null || callback.Result == null) throw new IllegalStateException("Invalid 4399 OAuth callback");
            String state = new ParameterBuilder(callback.Result).get("state");
            if (state.isEmpty()) throw new IllegalStateException("4399 OAuth callback did not contain state");
            return state;
        });
    }

    private CompletableFuture<String> handleCaptchaWithHtml(String username, String password, String html, int retry) {
        var match = CAPTCHA_ID.matcher(html);
        if (!match.find()) return CompletableFuture.failedFuture(new IllegalStateException("Cannot find captcha in 4399 login HTML"));
        String id = match.group(1);
        return login.getAsync("/ptlogin/captcha.do?captchaId=" + DotNetUri.escapeDataString(id) + "&xx=1").thenCompose(response -> {
            HttpWrapper.ensureSuccess(response);
            return CompletableFuture.completedFuture(captchaRecognizer.recognize(response.body()));
        }).thenCompose(answer -> loginAndAuthorize(username, password, answer, id, retry + 1));
    }

    private void saveDevice() {
        synchronized (deviceLock) {
            try { Files.writeString(deviceFile, DotNetJson.serializeRelaxed(device), StandardCharsets.UTF_8); }
            catch (IOException e) { throw new UncheckedIOException(e); }
        }
    }
    private static String generateIdentifier() {
        try {
            String seed = UUID.randomUUID() + LocalDateTime.now().toString();
            String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(seed.getBytes(StandardCharsets.UTF_8)));
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")) + hash.substring(0, 50);
        } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    @Override public void close() {
        captchaRecognizer.close(); captchaHandler.close(); api.close(); login.close(); authorization.close(); sdk.close();
    }
}
