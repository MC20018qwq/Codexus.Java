package codexus.gateway.utils;

import codexus.csharpsup.*;
import codexus.game.launcher.utils.PathUtil;
import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.*;

/** Original website, settings, skin files, and ZIP thumbnail HTTP routes. */
public final class HostingUtil implements AutoCloseable {
    private final Path website, skins, settings;
    private final HttpServer server;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    public HostingUtil(Path website, Path skins, Path settings, int port) throws IOException {
        this.website = website.toAbsolutePath().normalize(); this.skins = skins.toAbsolutePath().normalize(); this.settings = settings.toAbsolutePath().normalize();
        if (!Files.isDirectory(this.website)) throw new FileNotFoundException("Missing website directory: " + website);
        Files.createDirectories(this.skins);
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), port), 0);
        server.setExecutor(executor); server.createContext("/", this::handle); server.start();
    }
    public static HostingUtil initWebsite() {
        try { return new HostingUtil(Path.of(PathUtil.WebSitePath), Path.of(PathUtil.SkinsPath), Path.of(PathUtil.BasePath, "web_settings.json"), 3550); }
        catch (IOException e) { System.getLogger(HostingUtil.class.getName()).log(System.Logger.Level.ERROR, "Unable to host Gateway website", e); return null; }
    }
    public int getPort() { return server.getAddress().getPort(); }
    private static Path safeFile(Path root, String name) throws IOException {
        Path value = root.resolve(name).normalize();
        if (!value.startsWith(root)) throw new IllegalArgumentException("Path is outside the resource directory");
        Path existing = value;
        while (existing != null && !Files.exists(existing, LinkOption.NOFOLLOW_LINKS)) existing = existing.getParent();
        if (existing == null || !existing.toRealPath().startsWith(root.toRealPath())) throw new IllegalArgumentException("Resource symlink escapes its directory");
        return value;
    }
    private void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            try {
                String path = exchange.getRequestURI().getPath(), method = exchange.getRequestMethod();
                boolean page = method.equals("GET") || method.equals("HEAD");
                if (path.equals("/api/settings")) {
                    if (page) { reply(exchange, 200, "application/json", Files.exists(settings) ? Files.readAllBytes(settings) : "{}".getBytes(StandardCharsets.UTF_8)); return; }
                    if (method.equals("POST")) {
                        var json = DotNetJson.parse(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                        if (json == null || !json.isObject()) throw new IllegalArgumentException("Settings must be a JSON object");
                        Files.createDirectories(settings.getParent()); Files.writeString(settings, DotNetJson.serializeRelaxedIndented(json));
                        reply(exchange, 200, "application/json", DotNetJson.serialize(Map.of("ok", true)).getBytes(StandardCharsets.UTF_8)); return;
                    }
                }
                if (page && path.equals("/api/skins")) {
                    Files.createDirectories(skins);
                    try (var files = Files.list(skins)) {
                        var names = files.filter(Files::isRegularFile).map(file -> file.getFileName().toString())
                                .filter(name -> name.toLowerCase(Locale.ROOT).endsWith(".png") || name.toLowerCase(Locale.ROOT).endsWith(".zip")).toList();
                        reply(exchange, 200, "application/json", DotNetJson.serialize(names).getBytes(StandardCharsets.UTF_8)); return;
                    }
                }
                if (page && path.equals("/api/skins/thumbnail")) {
                    String file = null, query = exchange.getRequestURI().getRawQuery();
                    if (query != null) for (String pair : query.split("&")) { var parts = pair.split("=", 2); if (parts[0].equals("file") && parts.length == 2) file = URLDecoder.decode(parts[1], StandardCharsets.UTF_8); }
                    if (file == null || file.isEmpty()) { reply(exchange, 400, "text/plain", new byte[0]); return; }
                    Path input = safeFile(skins, file);
                    if (!Files.isRegularFile(input)) { reply(exchange, 404, "text/plain", new byte[0]); return; }
                    if (file.toLowerCase(Locale.ROOT).endsWith(".zip")) {
                        try (var zip = new ZipFile(input.toFile())) {
                            var entry = zip.getEntry("skin.png");
                            if (entry == null) entry = zip.stream().filter(item -> !item.isDirectory() && item.getName().toLowerCase(Locale.ROOT).endsWith(".png")).findFirst().orElse(null);
                            if (entry == null) { reply(exchange, 404, "text/plain", new byte[0]); return; }
                            try (var stream = zip.getInputStream(entry)) { reply(exchange, 200, "image/png", stream.readAllBytes()); return; }
                        }
                    }
                    reply(exchange, 200, "image/png", Files.readAllBytes(input)); return;
                }
                if (page && path.startsWith("/skins/")) {
                    Path file = safeFile(skins, path.substring(7));
                    reply(exchange, Files.isRegularFile(file) ? 200 : 404, "application/octet-stream", Files.isRegularFile(file) ? Files.readAllBytes(file) : new byte[0]); return;
                }
                if (page && !path.equalsIgnoreCase("/api") && !path.toLowerCase(Locale.ROOT).startsWith("/api/")) {
                    Path file = safeFile(website, path.substring(1));
                    if (Files.isDirectory(file)) file = safeFile(website, website.relativize(file.resolve("index.html")).toString());
                    if (!Files.isRegularFile(file) && !path.substring(path.lastIndexOf('/') + 1).contains(".")) file = safeFile(website, "index.html");
                    if (Files.isRegularFile(file)) {
                        String type = Files.probeContentType(file);
                        reply(exchange, 200, type == null ? "application/octet-stream" : type, Files.readAllBytes(file)); return;
                    }
                }
                reply(exchange, path.equals("/api/settings") || path.equals("/api/skins") || path.equals("/api/skins/thumbnail") ? 405 : 404, "text/plain", new byte[0]);
            } catch (IllegalArgumentException e) { reply(exchange, 400, "application/json", DotNetJson.serialize(Map.of("error", String.valueOf(e.getMessage()))).getBytes(StandardCharsets.UTF_8)); }
            catch (IOException e) { reply(exchange, 500, "application/json", DotNetJson.serialize(Map.of("error", String.valueOf(e.getMessage()))).getBytes(StandardCharsets.UTF_8)); }
        }
    }
    private static void reply(HttpExchange exchange, int code, String type, byte[] body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", type);
        if (exchange.getRequestMethod().equals("HEAD")) { exchange.getResponseHeaders().set("Content-Length", Integer.toString(body.length)); exchange.sendResponseHeaders(code, -1); }
        else { exchange.sendResponseHeaders(code, body.length == 0 ? -1 : body.length); exchange.getResponseBody().write(body); }
    }
    @Override public void close() { server.stop(0); executor.shutdownNow(); }
}
