package codexus.cipher.extensions.com4399extensions;

import codexus.cipher.utils.exception.CaptchaException;
import codexus.cipher.utils.http.HttpWrapper;
import codexus.csharpsup.DotNetJson;
import codexus.csharpsup.DotNetUri;
import java.awt.Desktop;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/** Presents challenges to a person, then asks 4399 to validate the supplied answer. */
public final class CaptchaHandler implements AutoCloseable {
    public enum CaptchaType { JIGSAW, CLICK }
    public record Challenge(CaptchaType type, String backgroundImageBase64, String sliderImageBase64,
                            String clickableText, String captchaId) { }
    /** Returns the protocol answer: encrypted jigsaw data or click JSON. */
    @FunctionalInterface public interface Prompt {
        CompletableFuture<String> solve(Challenge challenge);
    }

    private final HttpWrapper http;
    private final String checkBase;
    private final Prompt prompt;
    private final Set<CompletableFuture<?>> pending = ConcurrentHashMap.newKeySet();
    private volatile boolean closed;

    public CaptchaHandler() { this(new HttpWrapper(), "https://m.4399api.com", CaptchaHandler::openBrowser); }
    public CaptchaHandler(HttpWrapper http, String checkBase, Prompt prompt) {
        this.http = Objects.requireNonNull(http);
        this.checkBase = Objects.requireNonNull(checkBase);
        this.prompt = Objects.requireNonNull(prompt);
    }

    public CompletableFuture<String> handleLoginCaptchaAsync(String resultJson) {
        if (resultJson == null || resultJson.isEmpty()) return CompletableFuture.completedFuture(resultJson);
        var root = DotNetJson.parse(resultJson);
        if (!"103".equals(root.path("code").asText())) return CompletableFuture.completedFuture(resultJson);
        String url = root.path("result").path("url").asText();
        CaptchaType type;
        if (url.contains("jigsaw")) type = CaptchaType.JIGSAW;
        else if (url.contains("click")) type = CaptchaType.CLICK;
        else return CompletableFuture.failedFuture(new CaptchaException("Unsupported 4399 captcha challenge"));
        return solveChallenge(url, type, 0);
    }

    private CompletableFuture<String> solveChallenge(String url, CaptchaType type, int retry) {
        if (closed) return CompletableFuture.failedFuture(new IllegalStateException("Captcha handler is closed"));
        if (retry > 5) return CompletableFuture.failedFuture(new CaptchaException("Captcha retry limit exceeded"));
        return http.getAsync(url).thenCompose(response -> {
            HttpWrapper.ensureSuccess(response);
            var data = DotNetJson.parse(HttpWrapper.readAsString(response)).path("result");
            var challenge = new Challenge(type, data.path("img").asText(), data.path("img2").asText(),
                    data.path("text").asText(), data.path("captchaId").asText());
            if (challenge.backgroundImageBase64().isEmpty() || challenge.captchaId().isEmpty()
                    || (type == CaptchaType.JIGSAW && challenge.sliderImageBase64().isEmpty())
                    || (type == CaptchaType.CLICK && challenge.clickableText().isEmpty()))
                throw new CaptchaException("Incomplete 4399 captcha challenge");
            return present(challenge).thenCompose(answer -> {
                String path = type == CaptchaType.JIGSAW ? "jigsaw" : "click";
                String check = checkBase + "/captcha/" + path + "-check.html?refer=sdk&v="
                        + DotNetUri.escapeDataString(answer) + "&captchaId=" + DotNetUri.escapeDataString(challenge.captchaId());
                return http.getAsync(check).thenCompose(checked -> {
                    HttpWrapper.ensureSuccess(checked);
                    var root = DotNetJson.parse(HttpWrapper.readAsString(checked));
                    if (!root.path("code").isIntegralNumber() || root.path("code").asInt() != 100)
                        return solveChallenge(url, type, retry + 1);
                    String token = root.path("result").path("token").asText();
                    if (token.isEmpty()) throw new CaptchaException("4399 did not return a captcha token");
                    var parameter = new java.util.LinkedHashMap<String, String>();
                    parameter.put("v_token", token); parameter.put("captcha_id", challenge.captchaId()); parameter.put("type", "0");
                    return CompletableFuture.completedFuture(DotNetJson.serialize(parameter));
                });
            });
        });
    }

    private CompletableFuture<String> present(Challenge challenge) {
        if (closed) return CompletableFuture.failedFuture(new IllegalStateException("Captcha handler is closed"));
        CompletableFuture<String> answer;
        try { answer = Objects.requireNonNull(prompt.solve(challenge)); }
        catch (Exception e) { return CompletableFuture.failedFuture(e); }
        pending.add(answer);
        if (closed) answer.cancel(true);
        var result = answer.thenApply(value -> {
            if (value == null || value.isBlank()) throw new CaptchaException("Captcha answer is empty");
            return value;
        });
        result.whenComplete((value, failure) -> {
            pending.remove(answer);
            if (result.isCancelled()) answer.cancel(true);
        });
        return result;
    }

    private static CompletableFuture<String> openBrowser(Challenge challenge) {
        CaptchaHttpServer server;
        try {
            server = new CaptchaHttpServer(0, challenge);
            server.startAsync().join();
        } catch (Exception e) { return CompletableFuture.failedFuture(e); }
        var result = server.getResult().orTimeout(300, TimeUnit.SECONDS);
        result.whenComplete((answer, failure) -> server.close());
        try {
            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
                throw new IllegalStateException("Desktop browser unavailable; supply a CaptchaHandler.Prompt from the host UI");
            Desktop.getDesktop().browse(server.getUri());
        } catch (Exception e) { result.completeExceptionally(e); }
        return result;
    }

    /** Same MD5-derived AES-CBC/PKCS7 format as the original CryptoJS page. */
    public static String encryptJigsawSolution(String json) {
        try {
            byte[] key = MessageDigest.getInstance("MD5").digest("df0a5f98c337de97".getBytes(StandardCharsets.UTF_8));
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"),
                    new IvParameterSpec("79c83220d9974edf".getBytes(StandardCharsets.UTF_8)));
            return Base64.getEncoder().encodeToString(cipher.doFinal(json.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) { throw new IllegalStateException("Cannot encode jigsaw solution", e); }
    }

    @Override public void close() {
        closed = true;
        pending.forEach(future -> future.cancel(true));
        http.close();
    }
}
