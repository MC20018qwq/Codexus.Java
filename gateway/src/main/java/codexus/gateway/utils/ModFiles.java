package codexus.gateway.utils;

import codexus.game.launcher.utils.PathUtil;
import java.io.IOException;
import java.nio.file.*;
import java.util.Locale;

public final class ModFiles {
    private ModFiles() { }
    public static Path directory() throws IOException {
        Path root = Path.of(PathUtil.CustomModsPath).toAbsolutePath().normalize();
        Files.createDirectories(root); return root.toRealPath();
    }
    public static Path resolve(String value, boolean fileNameOnly) throws IOException {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Empty mod path");
        Path root = directory(), path = Path.of(value);
        if (fileNameOnly && (path.isAbsolute() || path.getNameCount() != 1 || value.contains("/") || value.contains("\\")))
            throw new IllegalArgumentException("Mod name must be a file name");
        for (Path part : path) if (part.toString().equals("..")) throw new IllegalArgumentException("Invalid mod path");
        Path result = (path.isAbsolute() ? path : root.resolve(path)).toAbsolutePath().normalize();
        if (!result.startsWith(root) || result.equals(root) || !result.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar"))
            throw new IllegalArgumentException("Only JAR files in the custom mods directory are allowed");
        Path existing = result;
        while (existing != null && !Files.exists(existing, LinkOption.NOFOLLOW_LINKS)) existing = existing.getParent();
        if (existing == null || !existing.toRealPath().startsWith(root)) throw new IllegalArgumentException("Mod path escapes its directory");
        return result;
    }
}
