package codexus.cipher.utils.http;

import codexus.csharpsup.*;
import java.net.*;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class HttpWrapper implements AutoCloseable {
    public static class HttpWrapperBuilder {
        private final Map<String, String> headers = new DotNetDictionary<>();
        public final String Domain, Url, Body;
        public HttpWrapperBuilder(String domain, String url, String body) { Domain = domain; Url = url; Body = body; }
        public HttpWrapperBuilder addHeader(Map<String, String> values) { values.forEach(this::addHeader); return this; }
        public HttpWrapperBuilder addHeader(String key, String value) {
            if (headers.containsKey(key)) throw new IllegalArgumentException("An item with the same key has already been added: " + key);
            headers.put(key, value); return this;
        }
        public HttpWrapperBuilder userAgent(String value) { return addHeader("User-Agent", value); }
        public Map<String, String> getHeaders() { return headers; }
    }
    private final HttpClient client;
    private final String domain;
    private final Consumer<HttpWrapperBuilder> extension;
    private final HttpClient.Version version;
    public HttpWrapper() { this(""); }
    public HttpWrapper(String domain) { this(domain, null, null, null); }
    public HttpWrapper(String domain, Consumer<HttpWrapperBuilder> extension) { this(domain, extension, null, null); }
    public HttpWrapper(String domain, Consumer<HttpWrapperBuilder> extension, HttpClient client, HttpClient.Version version) {
        this.domain = domain; this.extension = extension; this.version = version;
        this.client = client == null ? HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
                .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL)).followRedirects(HttpClient.Redirect.ALWAYS).build() : client;
    }
    public HttpClient getClient() { return client; }
    public CompletableFuture<HttpResponse<byte[]>> postAsync(String url, String body) { return postAsync(url, body, "application/json", null); }
    public CompletableFuture<HttpResponse<byte[]>> postAsync(String url, String body, Consumer<HttpWrapperBuilder> block) {
        return postAsync(url, body, "application/json", block);
    }
    public CompletableFuture<HttpResponse<byte[]>> postAsync(String url, String body, String contentType) { return postAsync(url, body, contentType, null); }
    public CompletableFuture<HttpResponse<byte[]>> postAsync(String url, String body, String contentType, Consumer<HttpWrapperBuilder> block) {
        var builder = request(url, body, block).header("Content-Type", contentType + "; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofByteArray(DotNetEncoding.utf8(body)));
        return send(builder.build());
    }
    public CompletableFuture<HttpResponse<byte[]>> postAsync(String url, byte[] body) { return postAsync(url, body, null); }
    public CompletableFuture<HttpResponse<byte[]>> postAsync(String url, byte[] body, Consumer<HttpWrapperBuilder> block) {
        return send(request(url, DotNetEncoding.utf8(body), block).POST(HttpRequest.BodyPublishers.ofByteArray(body)).build());
    }
    public CompletableFuture<HttpResponse<byte[]>> getAsync(String url) { return getAsync(url, null); }
    public CompletableFuture<HttpResponse<byte[]>> getAsync(String url, Consumer<HttpWrapperBuilder> block) {
        return send(request(url, "", block).GET().build());
    }
    private HttpRequest.Builder request(String url, String body, Consumer<HttpWrapperBuilder> block) {
        var builder = HttpRequest.newBuilder(URI.create(domain + url)).timeout(Duration.ofSeconds(100));
        builder.version(version == null ? HttpClient.Version.HTTP_1_1 : version);
        if (extension != null || block != null) {
            var values = new HttpWrapperBuilder(domain, url, body);
            if (extension != null) extension.accept(values);
            if (block != null) block.accept(values);
            values.getHeaders().forEach(builder::header);
        }
        return builder;
    }
    private CompletableFuture<HttpResponse<byte[]>> send(HttpRequest request) {
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray()).exceptionallyCompose(error ->
                CompletableFuture.failedFuture(new HttpRequestException("HTTP request failed", error)));
    }
    public static void ensureSuccess(HttpResponse<?> response) {
        if (response.statusCode() < 200 || response.statusCode() >= 300)
            throw new HttpRequestException("Response status code does not indicate success: " + response.statusCode(), response.statusCode());
    }
    public static String readAsString(HttpResponse<byte[]> response) {
        String type = response.headers().firstValue("Content-Type").orElse("");
        java.nio.charset.Charset charset = java.nio.charset.StandardCharsets.UTF_8;
        var match = java.util.regex.Pattern.compile("(?i)charset\\s*=\\s*\"?([^;\"\\s]+)").matcher(type);
        if (match.find()) charset = java.nio.charset.Charset.forName(match.group(1));
        byte[] bytes = response.body();
        int offset = 0;
        if (bytes.length >= 3 && (bytes[0] & 255) == 239 && (bytes[1] & 255) == 187 && (bytes[2] & 255) == 191) offset = 3;
        else if (bytes.length >= 2 && (bytes[0] & 255) == 255 && (bytes[1] & 255) == 254) {
            charset = java.nio.charset.StandardCharsets.UTF_16LE; offset = 2;
        } else if (bytes.length >= 2 && (bytes[0] & 255) == 254 && (bytes[1] & 255) == 255) {
            charset = java.nio.charset.StandardCharsets.UTF_16BE; offset = 2;
        }
        return new String(bytes, offset, bytes.length - offset, charset);
    }
    @Override public void close() { client.shutdownNow(); }
}
