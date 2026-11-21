package codexus.gateway.message.connected.plugin;

import codexus.csharpsup.DotNetJson;
import codexus.development.sdk.manager.PluginManager;
import codexus.development.sdk.utils.WebNexusApi;
import codexus.game.launcher.utils.PathUtil;
import codexus.gateway.connection.*;
import codexus.gateway.entities.web.gateway.*;
import codexus.gateway.handler.ClientHandler;
import codexus.gateway.manager.TokenManager;
import codexus.gateway.message.*;
import codexus.gateway.utils.PluginArchive;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import static codexus.gateway.handler.ClientHandler.send;

@RegisterMessage(state = ConnectionState.Connected, type = "install_plugin")
public final class InstallPluginMessage extends JsonMessage<EntityInstallPluginRequest> {
    private static final ReentrantLock INSTALL_LOCK = new ReentrantLock();
    public boolean LoadNow = true;
    public Consumer<Set<String>> OnInstalled;
    public InstallPluginMessage() { super(EntityInstallPluginRequest.class); }
    @Override public void handleMessage(ClientHandler handler, SecureConnection connection) {
        if (!INSTALL_LOCK.tryLock()) { send(connection, "error_notification", "Another plugin installation is in progress"); return; }
        Path staging = null; ScheduledExecutorService reporter = null;
        var progress = new ConcurrentHashMap<String, Integer>();
        try {
            if (request == null || request.Plugin == null) throw new IllegalArgumentException("Invalid plugin installation request");
            var plugins = request.Plugin.getAllDownloadPlugins();
            plugins.forEach(plugin -> progress.put(plugin.Id, 0));
            reporter = Executors.newSingleThreadScheduledExecutor(Thread.ofPlatform().daemon(true).name("gateway-plugin-progress").factory());
            String identify = connection.CurrentIdentify.get();
            reporter.scheduleAtFixedRate(() -> {
                connection.CurrentIdentify.set(identify);
                try {
                int percent = (int) progress.values().stream().mapToInt(Integer::intValue).average().orElse(100);
                report(connection, percent, false);
                } finally { connection.CurrentIdentify.remove(); }
            }, 0, 50, TimeUnit.MILLISECONDS);
            Path directory = Path.of(PathUtil.BasePath, "plugins"); Files.createDirectories(directory);
            staging = Files.createTempDirectory(directory, ".install-");
            var archives = new LinkedHashMap<EntityInstallPlugin, Path>(); var installed = new HashSet<String>();
            var manager = PluginManager.Instance;
            try (var api = new WebNexusApi(TokenManager.Instance.getToken(request.Id), (id, value) -> progress.computeIfPresent(id, (key, old) -> Math.clamp(value, 0, 100)))) {
                for (var plugin : plugins) {
                    synchronized (manager) {
                        if (manager.hasPlugin(plugin.Id)) {
                            if (plugin.Version.equals(manager.getPlugin(plugin.Id).Version)) {
                                installed.add(plugin.Id.toUpperCase(Locale.ROOT)); progress.put(plugin.Id, 100); continue;
                            }
                            LoadNow = false;
                        }
                    }
                    byte[] bytes = api.downloadFile(plugin.DownloadUrl, plugin.Id);
                    PluginArchive.verifyBytes(plugin, bytes);
                    Path file = staging.resolve(plugin.FileHash.toLowerCase(Locale.ROOT) + ".ug");
                    Files.write(file, bytes); archives.put(plugin, file); progress.put(plugin.Id, 100);
                }
            }
            PluginArchive.verifyMetadata(archives);
            synchronized (manager) {
                for (var entry : archives.entrySet()) {
                    Files.move(entry.getValue(), directory.resolve(entry.getValue().getFileName()), StandardCopyOption.REPLACE_EXISTING);
                }
                if (!LoadNow) {
                    for (var plugin : plugins) {
                        if (!installed.contains(plugin.Id.toUpperCase(Locale.ROOT)) && manager.hasPlugin(plugin.Id)) manager.uninstallPlugin(plugin.Id);
                        QueryInstallMessage.waitForRestart(plugin.Id);
                    }
                } else {
                    manager.loadPlugins(directory.toString());
                    for (var plugin : plugins) {
                        if (!manager.hasPlugin(plugin.Id) || !manager.getPlugin(plugin.Id).IsInitialized)
                            throw new IllegalStateException("Java plugin initialization failed: " + plugin.Id);
                    }
                }
                if (OnInstalled != null) OnInstalled.accept(Set.copyOf(installed));
            }
            stopReporter(reporter); reporter = null;
            report(connection, 100, true);
        } catch (Exception error) {
            stopReporter(reporter); reporter = null;
            if (request != null && request.Plugin != null) report(connection, 0, false);
            ClientHandler.error(connection, error);
        } finally {
            stopReporter(reporter);
            if (staging != null) {
                try (var entries = Files.list(staging)) { for (var file : entries.toList()) Files.deleteIfExists(file); }
                catch (java.io.IOException error) { System.getLogger(getClass().getName()).log(System.Logger.Level.WARNING, "Unable to remove plugin staging files", error); }
                try { Files.deleteIfExists(staging); } catch (java.io.IOException ignored) { }
            }
            INSTALL_LOCK.unlock();
        }
        if (request != null && request.Plugin != null) {
            var query = new EntityQueryInstall(); query.PluginId = request.Plugin.Id; query.PluginVersion = "any";
            new QueryInstallMessage(query).handleMessage(handler, connection);
        }
    }
    private static void stopReporter(ScheduledExecutorService reporter) {
        if (reporter != null) {
            reporter.shutdownNow();
            try { reporter.awaitTermination(2, TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }
    private void report(SecureConnection connection, int progress, boolean status) {
        send(connection, "report_install_plugin_progress", DotNetJson.serialize(Map.of("id", request.Plugin.Id, "progress", progress, "status", status)));
    }
}
