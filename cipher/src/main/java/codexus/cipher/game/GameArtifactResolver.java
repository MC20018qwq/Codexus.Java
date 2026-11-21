package codexus.cipher.game;

import codexus.csharpsup.WindowsCommandLine;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.zip.ZipFile;

/** Resolves the installed NetEase authentication inputs without loading any game classes. */
public final class GameArtifactResolver {
    private static final String LEGACY_LAUNCHER = "net/minecraft/launchwrapper/Launch.class";
    private static final String MODERN_LAUNCHER = "cpw/mods/modlauncher/Launcher.class";
    private final Path minecraft;
    private final ObjectMapper mapper = new ObjectMapper().enable(JsonParser.Feature.ALLOW_TRAILING_COMMA);

    public record GameArtifacts(Path bootstrapJar, Path datFile, String bootstrapMd5, String datFileMd5) { }

    public GameArtifactResolver(Path directory) {
        Path root = Objects.requireNonNull(directory, "minecraft directory").toAbsolutePath().normalize();
        minecraft = !Files.isDirectory(root.resolve("versions")) && Files.isDirectory(root.resolve(".minecraft/versions"))
                ? root.resolve(".minecraft") : root;
    }

    public static Path defaultMinecraftDirectory() {
        return Path.of(".game_cache", "Game", "Base", ".minecraft").toAbsolutePath().normalize();
    }

    public GameArtifacts resolve(String version) {
        try {
            checkedVersion(version);
            Path dat = minecraft.resolve(Path.of("versions", version, version + ".dat"));
            if (!Files.isRegularFile(dat)) throw new FileNotFoundException("NetEase version data not installed: " + dat);
            var configurations = new ArrayList<JsonNode>();
            collectConfigurations(version, configurations, new HashSet<>());
            String mainClass = configurations.stream().map(cfg -> cfg.path("mainClass").asText("")).filter(value -> !value.isEmpty()).findFirst().orElse("");
            String launcherClass = mainClass.equals("net.minecraft.launchwrapper.Launch") ? LEGACY_LAUNCHER : MODERN_LAUNCHER;
            var runtime = runtimeJars(configurations, version); var libraries = new LinkedHashSet<Path>();
            var libraryNames = new HashSet<String>();
            for (JsonNode cfg : configurations) collectLibraryPaths(cfg, libraries, libraryNames);
            Path launcher = findLauncher(runtime, launcherClass);
            if (launcher == null) launcher = findLauncher(libraries, launcherClass);
            if (launcher == null) throw new FileNotFoundException("No declared game JAR contains " + launcherClass + " for Minecraft " + version + " under " + minecraft);
            return new GameArtifacts(launcher, dat, md5(launcher), md5(dat));
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot resolve installed NetEase authentication files for " + version + ": " + e.getMessage(), e);
        }
    }

    /** Declared classpath/module-path JARs, including files that have not been installed yet. */
    public List<Path> declaredRuntimeJars(String version) throws IOException {
        var configurations = new ArrayList<JsonNode>();
        collectConfigurations(version, configurations, new HashSet<>());
        return runtimeJars(configurations, version);
    }

    private List<Path> runtimeJars(List<JsonNode> configurations, String version) throws IOException {
        var paths = new LinkedHashSet<Path>();
        for (JsonNode cfg : configurations) {
            if (!cfg.path("jvm_arguments").asText("").isBlank() || cfg.path("arguments").path("jvm").isArray()) {
                collectRuntimePaths(cfg, version, paths);
                break;
            }
        }
        return List.copyOf(paths);
    }

    private void collectConfigurations(String version, List<JsonNode> result, Set<String> visited) throws IOException {
        checkedVersion(version);
        if (!visited.add(version)) throw new IOException("Cyclic version inheritance: " + version);
        Path config = minecraft.resolve(Path.of("versions", version, version + ".json"));
        if (!Files.isRegularFile(config)) throw new FileNotFoundException("Game version JSON not installed: " + config);
        JsonNode cfg = mapper.readTree(Files.readAllBytes(config));
        if (cfg == null || !cfg.isObject()) throw new IOException("Invalid game version JSON: " + config);
        result.add(cfg);
        if (cfg.hasNonNull("inheritsFrom")) collectConfigurations(cfg.path("inheritsFrom").asText(), result, visited);
    }

    private static void checkedVersion(String version) {
        if (version == null || !version.matches("[A-Za-z0-9_.-]+") || version.equals(".") || version.equals(".."))
            throw new IllegalArgumentException("Invalid game version directory: " + version);
    }

    private void collectRuntimePaths(JsonNode cfg, String version, Set<Path> paths) throws IOException {
        List<String> args = new ArrayList<>(WindowsCommandLine.splitArguments(cfg.path("jvm_arguments").asText("")));
        if (args.isEmpty() && cfg.path("arguments").path("jvm").isArray())
            for (JsonNode arg : cfg.path("arguments").path("jvm")) if (arg.isTextual()) args.add(arg.asText());
        for (int i = 0; i < args.size(); i++) {
            String arg = args.get(i), value = null;
            if (Set.of("-cp", "-classpath", "--class-path", "-p", "--module-path").contains(arg) && i + 1 < args.size()) value = args.get(++i);
            else if (arg.startsWith("--class-path=") || arg.startsWith("--module-path=")) value = arg.substring(arg.indexOf('=') + 1);
            if (value == null) continue;
            value = value.replace("${library_directory}", "libraries").replace("${classpath_separator}", ";").replace("${version_name}", version);
            for (String entry : value.split(";")) if (!entry.contains("${") && entry.toLowerCase(Locale.ROOT).endsWith(".jar")) paths.add(insideRoot(entry));
        }
    }

    private void collectLibraryPaths(JsonNode cfg, Set<Path> paths, Set<String> names) throws IOException {
        for (JsonNode library : cfg.path("libraries")) {
            String coordinateName = library.path("name").asText("");
            String[] identity = coordinateName.split(":", -1);
            String key = identity.length >= 3 ? identity[0] + ":" + identity[1] + ":" + (identity.length > 3 ? identity[3] : "") : coordinateName;
            if (!key.isEmpty() && !names.add(key)) continue;
            String relative = library.path("downloads").path("artifact").path("path").asText("");
            if (relative.isEmpty()) {
                String[] coordinate = library.path("name").asText("").split(":", -1);
                if (coordinate.length < 3) continue;
                String version = coordinate[2], extension = "jar", classifier = coordinate.length > 3 ? coordinate[3] : "";
                int at = classifier.indexOf('@');
                if (at >= 0) { extension = classifier.substring(at + 1); classifier = classifier.substring(0, at); }
                at = version.indexOf('@');
                if (at >= 0) { extension = version.substring(at + 1); version = version.substring(0, at); }
                relative = coordinate[0].replace('.', '/') + "/" + coordinate[1] + "/" + version + "/"
                        + coordinate[1] + "-" + version + (classifier.isEmpty() ? "" : "-" + classifier) + "." + extension;
            }
            if (relative.toLowerCase(Locale.ROOT).endsWith(".jar")) paths.add(insideRoot("libraries/" + relative));
        }
    }

    private Path insideRoot(String relative) throws IOException {
        Path target = minecraft.resolve(relative.replace('\\', '/')).normalize();
        if (!target.startsWith(minecraft)) throw new IOException("Game library path escapes Minecraft directory: " + relative);
        return target;
    }

    private static Path findLauncher(Collection<Path> candidates, String entry) throws IOException {
        Path found = null;
        for (Path candidate : candidates) {
            if (!Files.isRegularFile(candidate)) continue;
            try (var jar = new ZipFile(candidate.toFile())) {
                if (jar.getEntry(entry) == null) continue;
                if (found != null && !Files.isSameFile(found, candidate))
                    throw new IOException("More than one declared JAR contains " + entry + ": " + found + ", " + candidate);
                found = candidate;
            }
        }
        return found;
    }

    private static String md5(Path path) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            try (InputStream input = Files.newInputStream(path)) {
                byte[] buffer = new byte[64 * 1024]; int size;
                while ((size = input.read(buffer)) != -1) digest.update(buffer, 0, size);
            }
            return HexFormat.of().withUpperCase().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) { throw new IllegalStateException("MD5 is unavailable", e); }
    }
}
