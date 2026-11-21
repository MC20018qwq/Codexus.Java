package codexus.cipher.protocol;

import codexus.cipher.entities.pc4399.Entity4399Response;
import codexus.cipher.utils.StringGenerator;
import codexus.cipher.utils.exception.CaptchaException;
import codexus.cipher.utils.http.HttpWrapper;
import codexus.cipher.utils.http.ParameterBuilder;
import codexus.csharpsup.DotNetJson;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/** 4399 PC account login; account creation is deliberately outside this protocol. */
public final class Pc4399 implements AutoCloseable {
    private static final String APP_ID = "kid_wdsj";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final String GAME_URL = "https://cdn.h5wan.4399sj.com/microterminal-h5-frame?game_id=500352";
    private final HttpWrapper login, service;
    private final MgbSdk sdk;
    public final MPay MPay;

    public Pc4399() {
        this(new MPay("aecfrxodyqaaaajp-g-x19", WPFLauncher.getLatestVersionAsync().join()),
                new HttpWrapper("https://ptlogin.4399.com", builder -> builder.userAgent(USER_AGENT)),
                new HttpWrapper("https://microgame.5054399.net", builder -> builder.userAgent(USER_AGENT)), new MgbSdk("x19"));
    }

    public Pc4399(MPay mpay, HttpWrapper login, HttpWrapper service, MgbSdk sdk) {
        MPay = Objects.requireNonNull(mpay);
        this.login = Objects.requireNonNull(login);
        this.service = Objects.requireNonNull(service);
        this.sdk = Objects.requireNonNull(sdk);
    }

    public CompletableFuture<String> loginWithPasswordAsync(String username, String password) {
        return loginWithPasswordAsync(username, password, null, null);
    }

    public CompletableFuture<String> loginWithPasswordAsync(String username, String password,
                                                            String captchaIdentifier, String captcha) {
        var parameters = buildParametersLogin().append("username", username).append("password", password);
        boolean initial = captchaIdentifier == null && captcha == null;
        CompletableFuture<Void> preflight = initial
                ? login.postAsync("/ptlogin/loginFrame.do?v=1", parameters.formUrlEncode(), "application/x-www-form-urlencoded")
                    .thenAccept(response -> {
                        if (HttpWrapper.readAsString(response).contains("账号异常，请输入验证码"))
                            throw new CaptchaException("^Captcha required^");
                    })
                : CompletableFuture.completedFuture(null);
        return preflight.thenCompose(ignored -> {
            if (captchaIdentifier != null && captcha != null)
                parameters.append("sessionId", captchaIdentifier).append("inputCaptcha", captcha);
            return login.postAsync("/ptlogin/login.do?v=1", parameters.formUrlEncode(), "application/x-www-form-urlencoded");
        }).thenCompose(response -> {
            HttpWrapper.ensureSuccess(response);
            return generateCookie();
        });
    }

    private CompletableFuture<String> generateCookie() {
        long timestamp = Instant.now().getEpochSecond();
        var parameters = new ParameterBuilder().append("appId", APP_ID).append("gameUrl", GAME_URL)
                .append("isCrossDomain", "1").append("nick", "null").append("onLineStart", "false")
                .append("ptLogin", "true").append("rand_time", "$randTime")
                .append("retUrl", "https://ptlogin.4399.com/resource/ucenter.html?action=login&appId=kid_wdsj&loginLevel=8&regLevel=8&bizId=2201001794&externalLogin=qq&qrLogin=true&layout=vertical&level=101&css=https://microgame.5054399.net/v2/resource/cssSdk/default/login.css&v=2018_11_26_16&postLoginHandler=redirect&checkLoginUserCookie=true&redirectUrl=http%3A%2F%2Fcdn.h5wan.4399sj.com%2Fmicroterminal-h5-frame%3Fgame_id%3D500352%26rand_time%3D" + timestamp)
                .append("show", "1");
        return login.getAsync("/ptlogin/checkKidLoginUserCookie.do?" + parameters.formUrlEncode())
                .thenCompose(response -> {
                    HttpWrapper.ensureSuccess(response);
                    String query = response.uri().getRawQuery();
                    if (query == null || query.isEmpty())
                        throw new IllegalStateException("4399 PC login did not return an authorization query");
                    return getUniAuth(query);
                }).thenApply(auth -> sdk.generateSAuth(MPay.Unique, auth.get("username"), auth.get("uid"),
                        auth.get("token"), auth.get("time"), "4399pc"));
    }

    private CompletableFuture<ParameterBuilder> getUniAuth(String query) {
        String callback = "jQuery1830" + StringGenerator.generateRandomString(16, true, false, false)
                + "_" + Instant.now().getEpochSecond();
        var parameters = new ParameterBuilder().append("callback", callback).append("queryStr", query);
        return service.getAsync("/v2/service/sdk/info?" + parameters.formUrlEncode()).thenApply(response -> {
            HttpWrapper.ensureSuccess(response);
            String text = HttpWrapper.readAsString(response).strip();
            if (text.endsWith(";")) text = text.substring(0, text.length() - 1).stripTrailing();
            String prefix = callback + "(";
            if (!text.startsWith(prefix) || !text.endsWith(")"))
                throw new IllegalStateException("Invalid 4399 PC authorization callback");
            var result = DotNetJson.deserialize(text.substring(prefix.length(), text.length() - 1), Entity4399Response.class);
            if (result == null || result.Data == null || result.Data.SdkLoginData == null || result.Data.SdkLoginData.isEmpty())
                throw new IllegalStateException("4399 PC authorization did not return SDK login data");
            var auth = new ParameterBuilder(result.Data.SdkLoginData);
            if (auth.get("uid").isEmpty() || auth.get("token").isEmpty())
                throw new IllegalStateException("4399 PC authorization did not return an account session");
            return auth;
        });
    }

    private static ParameterBuilder buildParametersLogin() {
        return new ParameterBuilder().append("appId", APP_ID).append("autoLogin", "on").append("bizId", "2201001794")
                .append("css", "https://microgame.5054399.net/v2/resource/cssSdk/default/login.css")
                .append("displayMode", "popup").append("externalLogin", "qq").append("gameId", "wd")
                .append("iframeId", "popup_login_frame").append("includeFcmInfo", "false").append("layout", "vertical")
                .append("layoutSelfAdapting", "true").append("level", "8").append("loginFrom", "uframe")
                .append("mainDivId", "popup_login_div").append("postLoginHandler", "default").append("redirectUrl", "")
                .append("regLevel", "8").append("sec", "1").append("sessionId", "")
                .append("userNameLabel", "4399用户名").append("userNameTip", "请输入4399用户名").append("welcomeTip", "欢迎回到4399");
    }

    @Override public void close() {
        MPay.close(); sdk.close(); login.close(); service.close();
    }
}
