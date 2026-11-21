package codexus.game.launcher.utils;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.*;
import java.util.*;
import java.util.concurrent.*;

public final class FileUtil {
    private FileUtil() { }
    private static boolean blank(String text) { return text == null || text.codePoints().allMatch(ch -> Character.isWhitespace(ch) || Character.isSpaceChar(ch)); }
    public static String[] enumerateFiles(String path) { return enumerateFiles(path, null); }
    public static String[] enumerateFiles(String path, String fileType) {
        if (blank(path) || !Files.isDirectory(Path.of(path))) return new String[0];
        String pattern = blank(fileType) ? "*" : "*." + fileType.replaceFirst("^\\.+", "").toLowerCase(Locale.ROOT);
        var matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        try (var files = Files.walk(Path.of(path))) {
            return files.filter(Files::isRegularFile).filter(file -> matcher.matches(file.getFileName())).map(Path::toString).toArray(String[]::new);
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    public static String computeMd5FromFile(String path) {
        if (blank(path) || !Files.isRegularFile(Path.of(path))) return "";
        try (var stream = Files.newInputStream(Path.of(path))) {
            var digest = MessageDigest.getInstance("MD5"); byte[] buffer = new byte[8192];
            for (int count; (count = stream.read(buffer)) >= 0;) digest.update(buffer, 0, count);
            return HexFormat.of().formatHex(digest.digest());
        } catch (IOException | SecurityException e) { return ""; }
        catch (NoSuchAlgorithmException e) { throw new AssertionError(e); }
    }
    public static boolean createSymbolicLinkSafe(String linkPath, String targetPath) {
        if (blank(linkPath) || blank(targetPath)) return false;
        try {
            Path link = Path.of(linkPath), target = Path.of(targetPath);
            if (Files.exists(link)) {
                if (Files.isSymbolicLink(link) && Files.readSymbolicLink(link).toAbsolutePath().normalize().equals(target.toAbsolutePath().normalize())) return true;
                Files.delete(link);
            }
            Files.createSymbolicLink(link, target); return true;
        } catch (IOException | SecurityException | UnsupportedOperationException e) { return false; }
    }
    public static void cleanDirectorySafe(String path) { if (!blank(path)) { deleteDirectorySafe(path); createDirectorySafe(path); } }
    public static boolean createDirectorySafe(String path) {
        if (blank(path)) return false;
        try { Files.createDirectories(Path.of(path)); return true; }
        catch (IOException | SecurityException e) { return false; }
    }
    public static boolean deleteDirectorySafe(String path) {
        if (blank(path) || !Files.isDirectory(Path.of(path))) return true;
        try {
            Files.walkFileTree(Path.of(path), new SimpleFileVisitor<>() {
                @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file); return FileVisitResult.CONTINUE;
                }
                @Override public FileVisitResult postVisitDirectory(Path dir, IOException error) throws IOException {
                    if (error != null) throw error;
                    Files.delete(dir); return FileVisitResult.CONTINUE;
                }
            });
            return true;
        } catch (IOException | SecurityException e) { return false; }
    }
    public static boolean copyFileSafe(String sourcePath, String destPath) {
        if (blank(sourcePath) || blank(destPath)) return false;
        try {
            Path destination = Path.of(destPath); if (destination.getParent() != null) Files.createDirectories(destination.getParent());
            Files.copy(Path.of(sourcePath), destination, StandardCopyOption.REPLACE_EXISTING); return true;
        } catch (IOException | SecurityException e) { return false; }
    }
    public static void copyDirectory(String sourceDir, String targetDir) { copyDirectory(sourceDir, targetDir, true, false); }
    public static void copyDirectory(String sourceDir, String targetDir, boolean includeRoot, boolean deleteSource) {
        if (blank(sourceDir) || !Files.isDirectory(Path.of(sourceDir))) return;
        Path source = Path.of(sourceDir), destination = includeRoot ? Path.of(targetDir).resolve(source.getFileName()) : Path.of(targetDir);
        try {
            Files.createDirectories(destination);
            try (var entries = Files.newDirectoryStream(source)) {
                for (Path entry : entries) {
                    Path target = destination.resolve(entry.getFileName());
                    if (Files.isDirectory(entry)) {
                        copyDirectory(entry.toString(), destination.toString(), true, deleteSource);
                        if (deleteSource) Files.delete(entry);
                    } else {
                        Files.copy(entry, target, StandardCopyOption.REPLACE_EXISTING);
                        if (deleteSource) Files.delete(entry);
                    }
                }
            }
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    public static boolean deleteFileSafe(String path) {
        try { if (path != null && Files.isRegularFile(Path.of(path))) Files.delete(Path.of(path)); return true; }
        catch (Exception e) { return false; }
    }
    public static boolean isFileReadable(String filePath) {
        try (var stream = Files.newInputStream(Path.of(filePath))) { return Files.size(Path.of(filePath)) > 0; }
        catch (Exception e) { return false; }
    }
    public static CompletableFuture<Void> writeFileSafelyAsync(String filePath, byte[] buffer) {
        return CompletableFuture.runAsync(() -> {
            Path target = Path.of(filePath), temporary = Path.of(filePath + ".tmp");
            try {
                Files.write(temporary, buffer);
                if (Files.exists(temporary) && Files.size(temporary) > 0) {
                    if (Files.isRegularFile(target)) Files.delete(target);
                    Files.move(temporary, target);
                }
            } catch (Exception e) {
                try { Files.deleteIfExists(temporary); } catch (IOException cleanup) { throw new UncheckedIOException(cleanup); }
                if (e instanceof IOException io) throw new UncheckedIOException(io);
                throw new CompletionException(e);
            }
        });
    }
}
