package codexus.game.launcher.services.java;

import codexus.game.launcher.utils.*;
import codexus.game.launcher.utils.progress.SyncProgressBarUtil.ProgressBar;
import java.net.URI;
import java.nio.file.*;
import java.util.concurrent.*;

public final class JreService {
    private JreService() { }
    private static final System.Logger LOG = System.getLogger(JreService.class.getName());
    private static final URI JAVA_CDN = URI.create("https://x19.gdl.netease.com/");
    private static boolean is64Bit() {
        return System.getProperty("os.arch").contains("64");
    }
    /** Prepare all runtimes supported by this OS, matching the launcher's Java repair action. */
    public static CompletableFuture<Boolean> prepareJavaRuntime() {
        return CompletableFuture.supplyAsync(() -> {
            for (int major : is64Bit() ? new int[]{8, 17, 22} : new int[]{8})
                if (!prepareJavaRuntime(major).join()) return false;
            return true;
        }, command -> Thread.ofVirtual().name("codexus-prepare-java").start(command));
    }
    public static CompletableFuture<Boolean> prepareJavaRuntime(int major) {
        return prepareJavaRuntime(major, Path.of(PathUtil.JavaPath), JAVA_CDN);
    }
    static CompletableFuture<Boolean> prepareJavaRuntime(int major, Path root, URI cdn) {
        String filename = JavaRuntimeUtil.archiveName(major, is64Bit());
        Path executable = JavaRuntimeUtil.executable(root, major), archive = root.resolve(filename);
        return CompletableFuture.supplyAsync(() -> {
            if (Files.isRegularFile(executable)) return true;
            try (var progress = new ProgressBar(100)) {
                Files.createDirectories(root);
                if (!DownloadUtil.downloadAsync(cdn.resolve(filename).toString(), archive.toString(), percent -> progress.update(percent, "Downloading Java " + major)).join()) return false;
                if (!CompressionUtil.extract7Z(archive.toString(), root.toString(), percent -> progress.update(percent, "Extracting Java " + major))) return false;
                if (!Files.isRegularFile(executable)) throw new IllegalStateException("Runtime archive is missing " + executable);
                Files.deleteIfExists(archive);
                return true;
            } catch (Exception e) {
                LOG.log(System.Logger.Level.ERROR, "Failed to prepare Java " + major, e);
                return false;
            } finally { ProgressBar.clearCurrent(); }
        }, command -> Thread.ofVirtual().name("codexus-prepare-java-" + major).start(command));
    }
}
