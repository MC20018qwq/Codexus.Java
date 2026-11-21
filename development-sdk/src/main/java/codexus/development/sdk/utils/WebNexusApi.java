package codexus.development.sdk.utils;

import codexus.csharpsup.*;
import codexus.development.sdk.game.GameCipherProvider;
import codexus.development.sdk.captcha.CaptchaRecognizer;
import java.util.function.Function;
import codexus.development.sdk.entities.BodyIn;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

public final class WebNexusApi implements AutoCloseable {
    private static final URI BASE = URI.create("https://api.codexus.today/");
    private final HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
    private final String authorization;
    private final BiConsumer<String, Integer> progress;
    private volatile boolean disposed;
    private GameCipherProvider gameCipher;
    private CaptchaRecognizer captchaRecognizer;
    public WebNexusApi(String nexusToken) { this(nexusToken, null); }
    public WebNexusApi(String nexusToken, BiConsumer<String, Integer> progressCallback) {
        this(nexusToken, progressCallback, null);
    }
    public WebNexusApi(String nexusToken, BiConsumer<String, Integer> progressCallback, GameCipherProvider gameCipher) {
        authorization = "Bearer " + (nexusToken == null ? "" : nexusToken);
        progress = progressCallback;
        this.gameCipher = gameCipher;
    }
    private static Map<String, Object> body(Object... pairs) {
        var result = new LinkedHashMap<String, Object>();
        for (int i = 0; i < pairs.length; i += 2) result.put((String) pairs[i], pairs[i + 1]);
        return result;
    }
    private synchronized GameCipherProvider localGameCipher() {
        if (disposed) throw new IllegalStateException("WebNexusApi has been disposed");
        if (gameCipher == null) {
            gameCipher = ServiceLoader.load(GameCipherProvider.class).findFirst().orElseThrow(() ->
                    new IllegalStateException("Local game computations require the cipher module on the runtime classpath."));
        }
        return gameCipher;
    }
    private CompletableFuture<String> localAsync(Function<GameCipherProvider, String> computation) {
        return CompletableFuture.supplyAsync(() -> computation.apply(localGameCipher()), Thread::startVirtualThread);
    }
    private static String envelope(String name, String value) { return DotNetJson.serialize(Map.of(name, value)); }

    public CompletableFuture<String> computeAuthenticationBodyAsync(String serverId, long gameId, String gameVersion,
            String modInfo, String channel, int userId, String handshakeKey) {
        return localAsync(local -> envelope("authBody", local.authenticationBody(serverId, gameId, gameVersion, modInfo, channel, userId, handshakeKey)));
    }
    public CompletableFuture<String> computeHandshakeBodyAsync(int userId, String token, String context, String channel, String version) {
        return localAsync(local -> envelope("handshakeBody", local.authenticationHandshake(userId, token, context, channel, version)));
    }
    // Existing PE API declarations are retained for compatibility; PE is excluded from migration.
    public CompletableFuture<String> peAccountConvert(String value) {
        return postAsync("/api/PeGameCipher/account/convert", body("body", value));
    }
    public CompletableFuture<String> peAuthentication(String clientKey, String displayName, String serverId, String gameType, long userId, String userToken) {
        if (userId < 0 || userId > 0xffffffffL) throw new IllegalArgumentException("userId is outside uint32 range");
        return postAsync("/api/PeGameCipher/authentication", body("clientKey", clientKey, "displayName", displayName,
                "serverId", serverId, "gameType", gameType, "userId", userId, "userToken", userToken));
    }
    public CompletableFuture<String> peHttpEncryptAsync(String value) { return postAsync("/api/PeGameCipher/crypto/encrypt", body("body", value)); }
    public CompletableFuture<String> peHttpDecryptAsync(String value) { return postAsync("/api/PeGameCipher/crypto/decrypt", body("body", value)); }
    public CompletableFuture<String> peCppGameClientInfoAsync(String os, long version, String entityId, long userId, String userToken) {
        return postAsync("/api/cpp-game-client-info", body("os", os, "version", version, "entityId", entityId, "userId", userId, "userToken", userToken));
    }
    public String peMcpGetCheckNum(String dynamicPyCode, String dynamicCheckSalt, String gamePlayerId) {
        return deserializeBody(postAsync("/api/PeZeroKnowledgeProof/zkp/get/check-num",
                body("dynamicPyCode", dynamicPyCode, "dynamicCheckSalt", dynamicCheckSalt, "gamePlayerId", gamePlayerId)).join());
    }
    public String peMcpGetStartType(String signature, String userId) {
        return deserializeBody(postAsync("/api/PeZeroKnowledgeProof/zkp/get/start-type", body("signature", signature, "userId", userId)).join());
    }
    public synchronized String computeCaptchaAsync(byte[] image) {
        if (disposed) throw new IllegalStateException("WebNexusApi has been disposed");
        if (captchaRecognizer == null) {
            captchaRecognizer = ServiceLoader.load(CaptchaRecognizer.class).findFirst().orElseThrow(() ->
                    new IllegalStateException("Local captcha recognition requires the cipher module on the runtime classpath."));
        }
        return captchaRecognizer.recognize(image);
    }
    public CompletableFuture<String> getAppVersionAsync(String appId, String appSecret) {
        return postAsync("/api/app/get/app/version", body("appId", appId, "appSecret", appSecret));
    }
    public CompletableFuture<String> getGatewayDownloadUrlAsync(String system) {
        return getAsync("/api/App/getGatewayDownloadUrl?system=" + DotNetUri.escapeDataString(system));
    }
    private HttpRequest.Builder request(String endpoint, Map<String, String> headers) {
        if (disposed) throw new IllegalStateException("WebNexusApi has been disposed");
        var builder = HttpRequest.newBuilder(BASE.resolve(endpoint)).timeout(Duration.ofSeconds(100))
                .header("Accept", "application/json").header("Authorization", authorization);
        if (headers != null) headers.forEach(builder::header);
        return builder;
    }
    private CompletableFuture<String> postAsync(String endpoint, Object requestData) {
        HttpRequest request = request(endpoint, null).header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofByteArray(DotNetEncoding.utf8(DotNetJson.serialize(requestData)))).build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
            if (response.statusCode() >= 200 && response.statusCode() < 300) return response.body();
            throw new IllegalStateException("Error processing API response: Request failed with status code "
                    + response.statusCode() + ". Response: " + response.body());
        });
    }
    public CompletableFuture<String> getAsync(String endpoint) { return getAsync(endpoint, null); }
    public CompletableFuture<String> getAsync(String endpoint, Map<String, String> headers) {
        return client.sendAsync(request(endpoint, headers).GET().build(), HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
            if (response.statusCode() < 200 || response.statusCode() >= 300)
                throw new CompletionException(new IOException("Request failed with status code " + response.statusCode() + ". Response: " + response.body()));
            return response.body();
        });
    }
    public byte[] downloadFile(String url, String pluginId) { return downloadFileAsync(url, pluginId, null).join(); }
    private CompletableFuture<byte[]> downloadFileAsync(String endpoint, String id, Map<String, String> headers) {
        return client.sendAsync(request(endpoint, headers).GET().build(), HttpResponse.BodyHandlers.ofInputStream()).thenApply(response -> {
            try (InputStream input = response.body(); var output = new ByteArrayOutputStream()) {
                if (response.statusCode() < 200 || response.statusCode() >= 300)
                    throw new IOException("Request failed with status code " + response.statusCode());
                long length = response.headers().firstValueAsLong("Content-Length").orElse(-1);
                long total = 0;
                byte[] buffer = new byte[8192];
                for (int count; (count = input.read(buffer)) > 0;) {
                    output.write(buffer, 0, count);
                    total += count;
                    if (length > 0 && progress != null) progress.accept(id, (int) (total * 100 / length));
                }
                if (length <= 0 && progress != null) progress.accept(id, 100);
                return output.toByteArray();
            } catch (IOException e) { throw new CompletionException(new IOException("Error processing API response: " + e.getMessage(), e)); }
        });
    }
    private static String deserializeBody(String json) {
        BodyIn result = DotNetJson.deserialize(json, BodyIn.class);
        if (result == null) throw new IllegalArgumentException("API returned an empty BodyIn payload.");
        return result.Body;
    }
    @Override public synchronized void close() {
        if (!disposed) { disposed = true; if (gameCipher != null) gameCipher.close(); if (captchaRecognizer != null) captchaRecognizer.close(); client.shutdownNow(); }
    }
}
