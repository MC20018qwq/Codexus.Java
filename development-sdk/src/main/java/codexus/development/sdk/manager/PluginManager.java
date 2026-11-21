package codexus.development.sdk.manager;

import codexus.csharpsup.*;
import codexus.development.sdk.attributes.Plugin;
import codexus.development.sdk.plugin.IPlugin;
import codexus.development.sdk.utils.ClassCatalog;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Predicate;

public class PluginManager implements AutoCloseable {
    private static final System.Logger LOG = System.getLogger(PluginManager.class.getName());
    private static final Path UNINSTALL_FILE = Path.of(".ug_cache");
    public static final PluginManager Instance = new PluginManager();
    private final Set<String> loadedFiles = new HashSet<>();
    private final Object writeFileLock = new Object();
    private final List<URLClassLoader> classLoaders = new ArrayList<>();
    public final Map<String, PluginState> Plugins = new LinkedHashMap<>();
    public static class PluginState {
        public final String Id, Name, Description, Version, Author, Path;
        public final String[] Dependencies;
        public final ClassLoader Assembly;
        public final IPlugin Plugin;
        public String Status = "Online";
        public boolean IsInitialized;
        public PluginState(String id, String name, String description, String version, String author,
                           String[] dependencies, String path, ClassLoader assembly, IPlugin plugin) {
            Id = id; Name = name; Description = description; Version = version; Author = author;
            Dependencies = dependencies; Path = path; Assembly = assembly; Plugin = plugin;
        }
    }
    public synchronized void ensureUninstall() {
        synchronized (writeFileLock) {
            try {
                if (!Files.exists(UNINSTALL_FILE)) {
                    Files.writeString(UNINSTALL_FILE, "[]");
                    return;
                }
                Set<String> paths = DotNetJson.deserialize(Files.readString(UNINSTALL_FILE), new TypeReference<LinkedHashSet<String>>() { });
                if (paths == null) return;
                for (String path : paths) Files.deleteIfExists(Path.of(path));
                Files.delete(UNINSTALL_FILE);
                Files.writeString(UNINSTALL_FILE, "[]");
            } catch (IOException e) { throw new UncheckedIOException(e); }
        }
    }
    public synchronized void loadPlugins(String directory) {
        Path dir = Path.of(directory);
        try {
            if (!Files.isDirectory(dir)) { Files.createDirectories(dir); return; }
            List<Path> files = new ArrayList<>();
            for (String glob : new String[]{"*.ug", "*.jar", "*.dll"}) {
                try (var entries = Files.newDirectoryStream(dir, glob)) { entries.forEach(files::add); }
            }
            List<URL> urls = new ArrayList<>();
            for (Path file : files) {
                if (!isClrLibrary(file) && !isExcludedRakNet(file)) urls.add(file.toUri().toURL());
            }
            var loader = new URLClassLoader(urls.toArray(URL[]::new), IPlugin.class.getClassLoader());
            classLoaders.add(loader);
            for (Path file : files) {
                String path = file.toString();
                if (loadedFiles.contains(path) || isExcludedRakNet(file)) continue;
                try {
                    if (isClrLibrary(file)) throw new UnsupportedOperationException("CLR plugin must be migrated to a Java JAR: " + file.getFileName());
                    for (Class<?> type : ClassCatalog.classes(file, loader)) {
                        if (!IPlugin.class.isAssignableFrom(type) || type.isInterface()
                                || java.lang.reflect.Modifier.isAbstract(type.getModifiers())) continue;
                        Plugin metadata = type.getDeclaredAnnotation(Plugin.class);
                        if (metadata == null) { LOG.log(System.Logger.Level.WARNING, "Plugin attribute missing: " + type.getName()); continue; }
                        String id = metadata.id().toUpperCase(Locale.ROOT);
                        if (Plugins.containsKey(id)) continue;
                        IPlugin plugin = (IPlugin) type.getConstructor().newInstance();
                        Plugins.put(id, new PluginState(id, metadata.name(), metadata.description(), metadata.version(),
                                metadata.author(), metadata.dependencies(), path, loader, plugin));
                        loadedFiles.add(path);
                    }
                } catch (Exception | LinkageError e) { LOG.log(System.Logger.Level.ERROR, "Failed to load plugin from " + file, e); }
            }
            checkDependencies();
            initializePlugins();
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    private static boolean isExcludedRakNet(Path path) {
        String name = path.getFileName().toString();
        return name.equalsIgnoreCase("Codexus.RakNet.dll") || name.equalsIgnoreCase("Codexus.RakNet.ug");
    }
    private static boolean isClrLibrary(Path path) {
        return path.toString().toLowerCase(Locale.ROOT).endsWith(".dll");
    }
    public synchronized boolean hasPlugin(String id) { return Plugins.containsKey(id.toUpperCase(Locale.ROOT)); }
    public synchronized PluginState getPlugin(String id) {
        PluginState state = Plugins.get(id.toUpperCase(Locale.ROOT));
        if (state == null) throw new IllegalStateException("Plugin " + id + " is not loaded");
        return state;
    }
    public synchronized List<String> getPluginAndDependencyPaths(String pluginId) { return getPluginAndDependencyPaths(pluginId, null); }
    public synchronized List<String> getPluginAndDependencyPaths(String pluginId, Predicate<String> excludeRule) {
        String id = pluginId.toUpperCase(Locale.ROOT);
        if (!hasPlugin(id)) throw new IllegalStateException("Plugin " + id + " is not loaded");
        Set<String> paths = new LinkedHashSet<>(), visited = new HashSet<>();
        collectDependencyPaths(id, paths, visited, excludeRule);
        return new ArrayList<>(paths);
    }
    private void collectDependencyPaths(String id, Set<String> paths, Set<String> visited, Predicate<String> exclude) {
        if (!visited.add(id) || (exclude != null && exclude.test(id))) return;
        if (!hasPlugin(id)) { LOG.log(System.Logger.Level.WARNING, "Plugin is not loaded: " + id); return; }
        PluginState state = getPlugin(id);
        paths.add(state.Path);
        if (state.Dependencies != null) for (String dependency : state.Dependencies)
            collectDependencyPaths(dependency.toUpperCase(Locale.ROOT), paths, visited, exclude);
    }
    private void checkDependencies() {
        for (PluginState state : Plugins.values()) {
            if (state.Dependencies == null) continue;
            for (String dependency : state.Dependencies) if (!hasPlugin(dependency))
                throw new IllegalStateException("Plugin " + state.Name + "(" + state.Id + ") depends on " + dependency + ", but it is not loaded");
        }
    }
    private static DotNetVersion parseVersion(String version) {
        try {
            String[] parts = version.split("\\.", -1);
            int[] values = new int[3];
            for (int i = 0; i < 3; i++) {
                if (i < parts.length) {
                    try { values[i] = Integer.parseInt(parts[i].strip()); }
                    catch (NumberFormatException e) { values[i] = 0; }
                }
                if (values[i] < 0) return new DotNetVersion(0, 0, 0, -1);
            }
            return new DotNetVersion(values[0], values[1], values[2], -1);
        } catch (Exception e) { return new DotNetVersion(0, 0, 0, -1); }
    }
    private void initializePlugins() {
        var selected = new LinkedHashMap<String, PluginState>();
        for (PluginState state : Plugins.values()) {
            String id = state.Id.toUpperCase(Locale.ROOT);
            PluginState existing = selected.get(id);
            if (existing == null || parseVersion(state.Version).compareTo(parseVersion(existing.Version)) > 0) selected.put(id, state);
        }
        Plugins.clear();
        for (PluginState state : selected.values()) Plugins.put(state.Id, state);
        Map<String, List<String>> dependents = new LinkedHashMap<>();
        Map<String, Integer> inDegree = new LinkedHashMap<>();
        for (PluginState state : Plugins.values()) { dependents.put(state.Id, new ArrayList<>()); inDegree.put(state.Id, 0); }
        for (PluginState state : Plugins.values()) {
            if (state.Dependencies == null) continue;
            for (String dependency : state.Dependencies) {
                String id = dependency.toUpperCase(Locale.ROOT);
                if (Plugins.containsKey(id)) { dependents.get(id).add(state.Id); inDegree.merge(state.Id, 1, Integer::sum); }
            }
        }
        Queue<String> queue = new ArrayDeque<>();
        inDegree.forEach((id, count) -> { if (count == 0) queue.add(id); });
        List<String> order = new ArrayList<>();
        while (!queue.isEmpty()) {
            String id = queue.remove(); order.add(id);
            for (String dependent : dependents.get(id)) {
                int remaining = inDegree.merge(dependent, -1, Integer::sum);
                if (remaining == 0) queue.add(dependent);
            }
        }
        if (order.size() != Plugins.size()) {
            var cycle = new LinkedHashSet<>(Plugins.keySet()); cycle.removeAll(order);
            throw new IllegalStateException("Circular dependency detected among plugins: " + String.join(", ", cycle));
        }
        for (String id : order) {
            PluginState state = Plugins.get(id);
            if (state.IsInitialized) continue;
            if (state.Assembly == null || state.Plugin == null)
                throw new IllegalStateException("Plugin " + state.Id + " is missing its loaded class loader or instance.");
            PacketManager.Instance.registerPacketClasses(ClassCatalog.classes(Path.of(state.Path), state.Assembly));
            state.Plugin.onInitialize();
            state.IsInitialized = true;
        }
    }
    public synchronized void uninstallPlugin(String pluginId) {
        PluginState state = Plugins.get(pluginId.toUpperCase(Locale.ROOT));
        if (state != null) { state.Status = "Waiting Restart"; uninstallPluginWithPaths(List.of(state.Path)); }
    }
    public synchronized void uninstallPluginWithPaths(List<String> paths) {
        synchronized (writeFileLock) {
            try {
                Set<String> pending = DotNetJson.deserialize(Files.readString(UNINSTALL_FILE), new TypeReference<LinkedHashSet<String>>() { });
                if (pending == null) { LOG.log(System.Logger.Level.ERROR, "Failed to read uninstall file"); return; }
                pending.addAll(paths);
                Files.writeString(UNINSTALL_FILE, DotNetJson.serialize(pending));
            } catch (IOException e) { throw new UncheckedIOException(e); }
        }
    }
    @Override public synchronized void close() {
        for (var loader : classLoaders) {
            try { loader.close(); }
            catch (IOException e) { LOG.log(System.Logger.Level.WARNING, "Failed to close plugin class loader", e); }
        }
        classLoaders.clear(); loadedFiles.clear(); Plugins.clear();
    }
    public static void restartGateway() {
        try {
            var info = ProcessHandle.current().info();
            String executable = info.command().orElse(null);
            if (executable == null || executable.isEmpty()) { LOG.log(System.Logger.Level.ERROR, "Failed to determine executable path."); return; }
            var arguments = new ArrayList<String>(); arguments.add(executable);
            info.arguments().ifPresent(args -> Collections.addAll(arguments, args));
            new ProcessBuilder(arguments).start();
            System.exit(0);
        } catch (Exception e) { LOG.log(System.Logger.Level.ERROR, "Failed to restart process", e); }
    }
}
