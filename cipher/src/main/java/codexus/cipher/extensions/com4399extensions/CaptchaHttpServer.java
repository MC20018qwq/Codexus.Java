package codexus.cipher.extensions.com4399extensions;

import codexus.cipher.extensions.com4399extensions.CaptchaHandler.Challenge;
import codexus.cipher.extensions.com4399extensions.CaptchaHandler.CaptchaType;
import codexus.csharpsup.DotNetJson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/** One loopback listener and result future per challenge; no shared captcha state. */
public final class CaptchaHttpServer implements AutoCloseable {
    private final HttpServer server;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final Challenge challenge;
    private final CompletableFuture<String> result = new CompletableFuture<>();
    private final AtomicBoolean started = new AtomicBoolean(), closed = new AtomicBoolean();
    public final int Port;

    public CaptchaHttpServer(int port, Challenge challenge) throws IOException {
        this.challenge = Objects.requireNonNull(challenge);
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        Port = server.getAddress().getPort();
        server.setExecutor(executor);
        server.createContext("/", this::handleRequest);
        result.whenComplete((answer, failure) -> close());
    }

    public CompletableFuture<Void> startAsync() {
        if (closed.get()) return CompletableFuture.failedFuture(new IllegalStateException("Captcha server is closed"));
        if (started.compareAndSet(false, true)) server.start();
        return CompletableFuture.completedFuture(null);
    }
    public URI getUri() { return URI.create("http://127.0.0.1:" + Port + "/"); }
    public CompletableFuture<String> getResult() { return result; }
    public void stop() { close(); }

    private void handleRequest(HttpExchange exchange) throws IOException {
        try (exchange) {
            String path = exchange.getRequestURI().getPath().toLowerCase(java.util.Locale.ROOT);
            if ((path.equals("/") || path.equals("/index.html")) && exchange.getRequestMethod().equals("GET")) {
                String html = switch (challenge.type()) {
                    case JIGSAW -> CaptchaResources.JigsawCaptchaHtml;
                    case CLICK -> CaptchaResources.ClickCaptchaHtml;
                };
                send(exchange, 200, "text/html; charset=utf-8", html);
            } else if (path.equals("/img.json") && exchange.getRequestMethod().equals("GET")) {
                Map<String, String> data = switch (challenge.type()) {
                    case JIGSAW -> Map.of("verificationImage", image(challenge.backgroundImageBase64()),
                            "sliderImage", image(challenge.sliderImageBase64()));
                    case CLICK -> Map.of("img", image(challenge.backgroundImageBase64()), "text", challenge.clickableText());
                };
                send(exchange, 200, "application/json", DotNetJson.serialize(data));
            } else if (path.equals("/resolve") && exchange.getRequestMethod().equals("POST")) {
                byte[] body = exchange.getRequestBody().readNBytes(65537);
                if (body.length > 65536) { send(exchange, 413, "text/plain", "Captcha answer too large"); return; }
                String answer;
                try {
                    var data = DotNetJson.parse(new String(body, StandardCharsets.UTF_8)).path("data");
                    if (!data.isTextual() || data.asText().isBlank()) throw new IllegalArgumentException();
                    answer = data.asText();
                    if (challenge.type() == CaptchaType.JIGSAW) answer = CaptchaHandler.encryptJigsawSolution(answer);
                } catch (RuntimeException e) { send(exchange, 400, "text/plain", "Invalid captcha answer"); return; }
                send(exchange, 200, "application/json", "{\"result\":\"resolved\"}");
                // Complete only after closing the response so a fast browser submission cannot lose its reply.
                exchange.close();
                result.complete(answer);
            } else send(exchange, 404, "text/plain", "Not Found");
        }
    }

    private static String image(String base64) { return "data:image/png;base64," + base64; }
    private static void send(HttpExchange exchange, int status, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }
    @Override public void close() {
        if (!closed.compareAndSet(false, true)) return;
        server.stop(0);
        executor.shutdownNow();
        result.cancel(false);
    }
}
