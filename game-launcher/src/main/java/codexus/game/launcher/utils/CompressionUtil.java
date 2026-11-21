package codexus.game.launcher.utils;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.IntConsumer;
import org.apache.commons.compress.archivers.sevenz.*;
import org.apache.commons.compress.archivers.zip.*;

public final class CompressionUtil {
    private CompressionUtil() { }
    public static boolean extract7Z(String filePath, String outPath, IntConsumer progress) {
        try { extractSevenZip(Path.of(filePath), Path.of(outPath), progress); return true; }
        catch (Exception first) {
            try { extractZip(Path.of(filePath), Path.of(outPath), progress); return true; }
            catch (Exception second) { return false; }
        }
    }
    public static CompletableFuture<Void> extract7ZAsync(String archivePath, String outputDir) {
        return extract7ZAsync(archivePath, outputDir, null);
    }
    public static CompletableFuture<Void> extract7ZAsync(String archivePath, String outputDir, IntConsumer progress) {
        return CompletableFuture.runAsync(() -> {
            try {
                Path input = Path.of(archivePath), output = Path.of(outputDir);
                byte[] signature;
                try (InputStream stream = Files.newInputStream(input)) { signature = stream.readNBytes(6); }
                if (SevenZFile.matches(signature, signature.length)) extractSevenZip(input, output, progress);
                else extractZip(input, output, progress);
            } catch (IOException e) { throw new UncheckedIOException(e); }
        }, command -> Thread.ofVirtual().name("codexus-extract").start(command));
    }
    private static void extractSevenZip(Path archivePath, Path output, IntConsumer progress) throws IOException {
        try (var archive = SevenZFile.builder().setPath(archivePath).get()) {
            List<SevenZArchiveEntry> entries = new ArrayList<>(); archive.getEntries().forEach(entries::add);
            int count = 0;
            for (var entry : entries) {
                if (entry.getName() != null && !entry.isDirectory()) {
                    Path destination = destination(output, entry.getName());
                    Files.createDirectories(destination.getParent());
                    try (InputStream input = archive.getInputStream(entry); OutputStream target = Files.newOutputStream(destination)) { input.transferTo(target); }
                }
                if (progress != null) progress.accept((int) (++count * 100.0 / entries.size()));
            }
        }
    }
    private static void extractZip(Path archivePath, Path output, IntConsumer progress) throws IOException {
        try (var archive = ZipFile.builder().setPath(archivePath).get()) {
            var entries = Collections.list(archive.getEntries()); int count = 0;
            for (var entry : entries) {
                if (entry.getName() != null && !entry.isDirectory()) {
                    Path destination = destination(output, entry.getName());
                    Files.createDirectories(destination.getParent());
                    try (InputStream input = archive.getInputStream(entry); OutputStream target = Files.newOutputStream(destination)) { input.transferTo(target); }
                }
                if (progress != null) progress.accept((int) (++count * 100.0 / entries.size()));
            }
        }
    }
    private static Path destination(Path output, String entry) throws IOException {
        Path root = output.toAbsolutePath().normalize();
        Path target = root.resolve(entry.replace('\\', '/')).normalize();
        if (!target.startsWith(root)) throw new IOException("Archive entry escapes destination: " + entry);
        // Do not allow an existing symlink/junction in the output tree to redirect extraction.
        Files.createDirectories(root); Path realRoot = root.toRealPath();
        Path parent = target.getParent();
        for (Path current = parent; current != null && current.startsWith(root); current = current.getParent()) {
            if (Files.exists(current) && !current.toRealPath().startsWith(realRoot)) throw new IOException("Archive path traverses a link outside destination");
        }
        if (Files.isSymbolicLink(target)) throw new IOException("Archive destination is a symbolic link");
        return target;
    }
}
