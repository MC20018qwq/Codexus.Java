package codexus.game.launcher.services.bedrock;

import codexus.csharpsup.*;
import codexus.game.launcher.utils.*;
import codexus.game.launcher.utils.progress.SyncProgressBarUtil.ProgressBar;
import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.concurrent.*;

public final class InstallerService {
    private InstallerService() { }
    public static CompletableFuture<Boolean> downloadMinecraftAsync() { return downloadMinecraftAsync("", 0, ""); }
    public static CompletableFuture<Boolean> downloadMinecraftAsync(String nexusToken, long userId, String userToken) {
        return CompletableFuture.supplyAsync(() -> {
            Path base = Path.of(PathUtil.CppGamePath), archive = base.resolve("mc_base.7z"), executable = base.resolve(Path.of("x64_mc", "Minecraft.Windows.exe")), versionFile = base.resolve(Path.of("x64_mc", "Ver.txt"));
            try {
                if (Files.isRegularFile(executable) && Files.isRegularFile(versionFile)) return true;
                try (var progress = new ProgressBar(100)) {
                    FileUtil.cleanDirectorySafe(base.toString());
                    ClientInfo info = fetchGameClientInfo();
                    if (info.url == null || info.url.isBlank()) throw new IllegalStateException("Failed to fetch game download info from server");
                    if (!DownloadUtil.downloadAsync(info.url, archive.toString(), percent -> progress.update(percent, "Downloading Minecraft Bedrock")).join()) return false;
                    progress.update(0, "Checking package validation");
                    if (info.md5 == null || info.md5.isBlank()) throw new IllegalArgumentException("Expected MD5 hash cannot be null or empty");
                    if (!FileUtil.computeMd5FromFile(archive.toString()).equalsIgnoreCase(info.md5)) return false;
                    progress.update(100, "Package validation succeeded");
                    CompressionUtil.extract7ZAsync(archive.toString(), base.toString(), percent -> progress.update(percent, "Extracting Minecraft Bedrock")).join();
                    if (!Files.isRegularFile(executable)) throw new IOException("Extracted package does not contain Minecraft.Windows.exe");
                    if (info.version != null && !info.version.isBlank()) {
                        Files.createDirectories(versionFile.getParent()); byte[] text = DotNetEncoding.utf8(info.version), output = new byte[text.length + 3];
                        output[0] = (byte) 239; output[1] = (byte) 187; output[2] = (byte) 191; System.arraycopy(text, 0, output, 3, text.length); Files.write(versionFile, output);
                    }
                    progress.update(0, "Cleaning up game resources"); Files.deleteIfExists(archive); progress.update(100, "Cleanup completed"); return true;
                }
            } catch (Exception e) { throw new CompletionException(new IllegalStateException("Failed to download and install Minecraft", e)); }
            finally { ProgressBar.clearCurrent(); }
        }, command -> Thread.ofVirtual().name("codexus-bedrock-install").start(command));
    }
    private record ClientInfo(String url, String md5, String version) { }
    private static ClientInfo fetchGameClientInfo() {
        try (var client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()) {
            var request = HttpRequest.newBuilder(URI.create("https://x19apigatewayobt.nie.netease.com/cpp-game-client-info"))
                    .timeout(Duration.ofSeconds(15)).header("Content-Type", "application/json; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"os\":\"10.0\",\"version\":300000000,\"entity_id\":null}")).build();
            var json = DotNetJson.parse(client.send(request, HttpResponse.BodyHandlers.ofString()).body());
            if (json.has("code") && json.path("code").asInt() == 0 && json.has("entity")) {
                String url = json.path("entity").path("url").isTextual() ? json.path("entity").path("url").asText() : null;
                if (url != null && !url.isBlank()) return new ClientInfo(url, json.path("entity").path("md5").asText(""), extractVersionFromUrl(url));
            }
        } catch (Exception e) { System.getLogger(InstallerService.class.getName()).log(System.Logger.Level.DEBUG, "Failed to fetch Bedrock client download metadata", e); }
        return new ClientInfo(null, null, null);
    }
    static String extractVersionFromUrl(String url) {
        if (url == null || url.isBlank()) return ""; int start = url.lastIndexOf("Publish_"); if (start < 0) return "";
        start += "Publish_".length(); int end = url.indexOf('_', start); return url.substring(start, end < 0 ? url.length() : end);
    }
}
