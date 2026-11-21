package codexus.game.launcher.services.bedrock;

import codexus.cipher.entities.wpflauncher.netgame.texture.EnumGType;
import codexus.development.sdk.raknet.*;
import codexus.development.sdk.utils.NetworkUtil;
import codexus.game.launcher.entities.*;
import codexus.game.launcher.utils.*;
import codexus.game.launcher.utils.progress.SyncProgressBarUtil.ProgressBar;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public final class LauncherService implements AutoCloseable {
    public final UUID Identifier = UUID.randomUUID();
    public final EntityLaunchPeGame Entity;
    private final Consumer<EntityProgressUpdate> progress;
    private final String userToken;
    private final IRakNetCreate factory;
    private final Object lifecycle = new Object();
    private final List<Consumer<UUID>> exitListeners = new CopyOnWriteArrayList<>();
    private volatile EntityProgressUpdate lastProgress;
    private volatile boolean disposed;
    private Process process;
    private IRakNet rakNet;
    private CompletableFuture<Void> launchTask;
    private static final System.Logger LOG = System.getLogger(LauncherService.class.getName());
    private LauncherService(EntityLaunchPeGame entity, String userToken, Consumer<EntityProgressUpdate> progress, IRakNetCreate factory) {
        Entity = Objects.requireNonNull(entity); this.userToken = Objects.requireNonNull(userToken); this.progress = Objects.requireNonNull(progress); this.factory = Objects.requireNonNull(factory);
        lastProgress = new EntityProgressUpdate(); lastProgress.Id = Identifier; lastProgress.Percent = 0; lastProgress.Message = "Initialized";
    }
    public static LauncherService createLauncher(EntityLaunchPeGame entity, String userToken, Consumer<EntityProgressUpdate> progress) {
        IRakNetCreate factory = ServiceLoader.load(IRakNetCreate.class).findFirst().orElseThrow(() -> new UnsupportedOperationException("Bedrock requires a Java IRakNetCreate provider. The excluded Codexus.RakNet.dll/.ug is never loaded."));
        return createLauncher(entity, userToken, progress, factory);
    }
    public static LauncherService createLauncher(EntityLaunchPeGame entity, String userToken, Consumer<EntityProgressUpdate> progress, IRakNetCreate factory) {
        var launcher = new LauncherService(entity, userToken, progress, factory);
        launcher.launchTask = CompletableFuture.runAsync(launcher::launchGame, command -> Thread.ofVirtual().name("codexus-bedrock-launch").start(command)); return launcher;
    }
    public EntityProgressUpdate getLastProgress() { return lastProgress; }
    public CompletableFuture<Void> getLaunchTask() { return launchTask; }
    public void addExitedListener(Consumer<UUID> listener) { exitListeners.add(Objects.requireNonNull(listener)); }
    public void removeExitedListener(Consumer<UUID> listener) { exitListeners.remove(listener); }
    private void launchGame() {
        try {
            if (disposed) return;
            if (Entity.getLaunchType() != EnumLaunchType.Custom) {
                updateProgress(5, "Installing game resources");
                if (!InstallerService.downloadMinecraftAsync(Entity.AccessToken, Long.parseLong(Entity.UserId), userToken).join()) throw new IllegalStateException("Failed to download Minecraft resources");
            }
            synchronized (lifecycle) {
                if (disposed) return;
                updateProgress(60, "Launching proxy");
                int gamePort = NetworkUtil.getAvailablePort(), proxyPort = NetworkUtil.getAvailablePort(gamePort + 1);
                if (gamePort == 0 || proxyPort == 0) throw new IllegalStateException("No available Bedrock proxy ports");
                long user = Long.parseLong(Entity.UserId); if (user < 0 || user > 0xffffffffL) throw new IllegalArgumentException("User ID is outside UInt32 range");
                rakNet = factory.create(Entity.ServerIp + ":" + Entity.ServerPort, Entity.AccessToken, Entity.GameId, user, userToken, Entity.GameName,
                        Entity.RoleName, gamePort, proxyPort, Entity.getGameType() == EnumGType.ServerGame, Entity.IsDomainGame || Entity.GameId.toLowerCase(Locale.ROOT).endsWith(":domaingame"));
                if (rakNet == null) throw new IllegalStateException("RakNet provider returned no proxy");
                updateProgress(70, "Launching game process");
                Path executable = Entity.getLaunchType() == EnumLaunchType.Custom && Entity.LaunchPath != null && !Entity.LaunchPath.isEmpty()
                        ? Path.of(Entity.LaunchPath, "Minecraft.Windows.exe") : Path.of(PathUtil.CppGamePath, "x64_mc", "Minecraft.Windows.exe");
                if (!Files.isRegularFile(executable)) throw new FileNotFoundException("Executable not found at " + executable);
                ConfigService.generateLaunchConfig(Entity.SkinPath, Entity.RoleName, Entity.GameId, gamePort, executable.toAbsolutePath().getParent().toString(), Entity.ItemIds);
                process = CommandService.startGame(executable.toString());
                process.onExit().thenRun(() -> {
                    if (disposed) return;
                    for (var listener : exitListeners) try { listener.accept(Identifier); } catch (Exception e) { LOG.log(System.Logger.Level.WARNING, "Game exit listener failed", e); }
                });
                updateProgress(100, "Running");
            }
        } catch (Exception e) {
            updateProgress(100, e instanceof CancellationException ? "Launch cancelled" : "Launch failed");
            synchronized (lifecycle) { if (rakNet != null) { rakNet.shutdown(); rakNet = null; } }
            LOG.log(System.Logger.Level.ERROR, "Error while launching Bedrock " + Entity.GameId, e);
            throw e instanceof RuntimeException runtime ? runtime : new CompletionException(e);
        }
    }
    private void updateProgress(int percent, String message) {
        if (disposed) return;
        var update = new EntityProgressUpdate(); update.Id = Identifier; update.Percent = percent; update.Message = message; lastProgress = update;
        try { progress.accept(update); if (percent == 100) ProgressBar.clearCurrent(); }
        catch (Exception e) { LOG.log(System.Logger.Level.WARNING, "Error reporting progress", e); }
    }
    public Process getProcess() { synchronized (lifecycle) { return disposed ? null : process; } }
    public void shutdownAsync() { close(); }
    @Override public void close() {
        synchronized (lifecycle) {
            if (disposed) return; disposed = true;
            try {
                if (process != null && process.isAlive()) {
                    ProcessUtil.closeMainWindow(process);
                    if (!process.waitFor(5, TimeUnit.SECONDS)) process.destroyForcibly().waitFor();
                }
            } catch (Exception e) { LOG.log(System.Logger.Level.WARNING, "Error disposing game process", e); }
            finally { process = null; }
            try { if (rakNet != null) rakNet.shutdown(); }
            catch (Exception e) { LOG.log(System.Logger.Level.WARNING, "Error shutting down RakNet", e); }
            finally { rakNet = null; }
        }
    }
}
