package codexus.gateway.utils;

import codexus.development.sdk.attributes.Plugin;
import codexus.development.sdk.plugin.IPlugin;
import codexus.development.sdk.utils.ClassCatalog;
import codexus.gateway.entities.web.gateway.EntityInstallPlugin;
import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.jar.JarFile;

/** Checks the downloaded Java artifact before scheduling replacement of an installed plugin. */
public final class PluginArchive {
    private PluginArchive() { }
    public static void verifyBytes(EntityInstallPlugin plugin, byte[] bytes) {
        if (plugin.FileHash == null || !plugin.FileHash.matches("(?i)[0-9a-f]{64}")) throw new IllegalArgumentException("Invalid plugin SHA-256");
        if (plugin.FileSize <= 0 || plugin.FileSize != bytes.length) throw new IllegalArgumentException("Plugin file size mismatch: " + plugin.Id);
        try {
            String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
            if (!hash.equalsIgnoreCase(plugin.FileHash)) throw new IllegalArgumentException("Plugin SHA-256 mismatch: " + plugin.Id);
        } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
        if (bytes.length < 4 || bytes[0] != 'P' || bytes[1] != 'K')
            throw new IllegalArgumentException("CLR plugin must be migrated to a Java JAR: " + plugin.Id);
    }
    public static void verifyMetadata(Map<EntityInstallPlugin, Path> archives) throws IOException {
        var urls = new ArrayList<java.net.URL>();
        for (var file : archives.values()) {
            try (var jar = new JarFile(file.toFile())) {
                if (jar.stream().noneMatch(entry -> entry.getName().endsWith(".class")))
                    throw new IllegalArgumentException("Plugin contains no Java classes: " + file.getFileName());
            }
            urls.add(file.toUri().toURL());
        }
        // Dependencies may also be provided by an installed Java plugin.
        Path installed = Path.of("plugins");
        if (Files.isDirectory(installed)) try (var files = Files.list(installed)) {
            for (var file : files.filter(p -> p.toString().endsWith(".jar") || p.toString().endsWith(".ug")).toList()) urls.add(file.toUri().toURL());
        }
        try (var loader = new URLClassLoader(urls.toArray(java.net.URL[]::new), IPlugin.class.getClassLoader())) {
            for (var entry : archives.entrySet()) {
                boolean found = false;
                for (var type : ClassCatalog.classes(entry.getValue(), loader)) {
                    var metadata = type.getDeclaredAnnotation(Plugin.class);
                    if (metadata == null || !IPlugin.class.isAssignableFrom(type) || java.lang.reflect.Modifier.isAbstract(type.getModifiers())) continue;
                    if (metadata.id().equalsIgnoreCase(entry.getKey().Id) && metadata.version().equals(entry.getKey().Version)) { found = true; break; }
                }
                if (!found) throw new IllegalArgumentException("No matching Java IPlugin/@Plugin metadata, or missing Java dependency: " + entry.getKey().Id);
            }
        }
    }
}
