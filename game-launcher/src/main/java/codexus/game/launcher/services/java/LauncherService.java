package codexus.game.launcher.services.java;

import codexus.cipher.connection.protocols.AuthLibProtocol;
import codexus.cipher.entities.wpflauncher.netgame.EnumGameVersion;
import codexus.cipher.entities.wpflauncher.netgame.mods.EntityModsList;
import codexus.cipher.entities.wpflauncher.netgame.texture.EnumGType;
import codexus.cipher.protocol.WPFLauncher;
import codexus.cipher.skip32.Skip32Cipher;
import codexus.cipher.utils.GameVersionConverter;
import codexus.cipher.utils.cipher.TokenUtil;
import codexus.csharpsup.*;
import codexus.development.sdk.utils.NetworkUtil;
import codexus.game.launcher.entities.*;
import codexus.game.launcher.services.java.rpc.GameRpcService;
import codexus.game.launcher.utils.*;
import codexus.game.launcher.utils.progress.SyncProgressBarUtil.ProgressBar;
import java.io.*;
import java.net.InetAddress;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public final class LauncherService implements AutoCloseable {
    private static final System.Logger LOG = System.getLogger(LauncherService.class.getName());
    public final EntityLaunchGame Entity;
    public final UUID Identifier = UUID.randomUUID();
    private final String userToken, protocolVersion;
    private final WPFLauncher wpf;
    private final Consumer<EntityProgressUpdate> progress;
    private final Skip32Cipher skip32 = new Skip32Cipher(DotNetEncoding.ascii("SaintSteve"));
    private final int socketPort = NetworkUtil.getAvailablePort(9876);
    private final List<Consumer<UUID>> exitListeners = new CopyOnWriteArrayList<>();
    private volatile Process gameProcess;
    private volatile EntityProgressUpdate lastProgress;
    private volatile boolean disposed;
    private AuthLibProtocol auth;
    private GameRpcService rpc;
    private CompletableFuture<Void> launchTask;
    private final Object lifecycle = new Object();
    private LauncherService(EntityLaunchGame entity, String userToken, WPFLauncher wpf, String protocolVersion, Consumer<EntityProgressUpdate> progress) {
        Entity = Objects.requireNonNull(entity); this.userToken = Objects.requireNonNull(userToken); this.wpf = Objects.requireNonNull(wpf);
        this.protocolVersion = Objects.requireNonNull(protocolVersion); this.progress = Objects.requireNonNull(progress);
        lastProgress = update(0, "Initialized");
    }
    public static LauncherService createLauncher(EntityLaunchGame entity, String userToken, WPFLauncher wpf, String protocolVersion, Consumer<EntityProgressUpdate> progress) {
        var service = new LauncherService(entity, userToken, wpf, protocolVersion, progress);
        service.launchTask = CompletableFuture.runAsync(service::launchGame, command -> Thread.ofVirtual().name("codexus-java-launch").start(command)); return service;
    }
    public Process getProcess() { return gameProcess; }
    public EntityProgressUpdate getLastProgress() { return lastProgress; }
    public CompletableFuture<Void> getLaunchTask() { return launchTask; }
    public void addExitedListener(Consumer<UUID> listener) { exitListeners.add(Objects.requireNonNull(listener)); }
    public void removeExitedListener(Consumer<UUID> listener) { exitListeners.remove(listener); }
    private EntityProgressUpdate update(int percent, String text) { var value = new EntityProgressUpdate(); value.Id = Identifier; value.Percent = percent; value.Message = text; return value; }
    private void checkOpen() { if (disposed) throw new CancellationException("Launcher closed"); }
    private void report(ProgressBar bar, int percent, String text) {
        checkOpen(); var value = update(percent, text); bar.update(percent, text); progress.accept(value); lastProgress = value;
    }
    private void launchGame() {
        try (var bar = new ProgressBar(100)) {
            try {
                report(bar, 5, "Installing game mods");
                EnumGameVersion version = GameVersionConverter.convert(Entity.GameVersionId);
                var installed = InstallerService.installGameModsWithRuntime(Entity.UserId, userToken, version, wpf, Entity.GameId, Entity.getGameType() == EnumGType.ServerGame).join();
                EntityModsList mods = installed.mods();
                report(bar, 15, "Preparing Java runtime");
                int javaVersion = installed.requiredJavaVersion();
                if (!JreService.prepareJavaRuntime(javaVersion).join()) throw new IllegalStateException("Java " + javaVersion + " runtime installation failed");
                report(bar, 30, "Preparing Minecraft client");
                if (!InstallerService.prepareMinecraftClient(Entity.UserId, userToken, wpf, version).join()) throw new IllegalStateException("Minecraft client installation failed");
                report(bar, 45, "Setting up runtime");
                Path working = Path.of(InstallerService.prepareGameRuntime(Entity.UserId, Entity.GameId, Entity.RoleName, Entity.getGameType()), ".minecraft");
                InstallerService.installNativeDll(version);
                report(bar, 60, "Applying core mods");
                Path modPath = working.resolve("mods");
                if (Entity.LoadCoreMods) InstallerService.installCoreMods(Entity.GameId, modPath.toString());
                else for (String file : FileUtil.enumerateFiles(modPath.toString(), "jar")) if (file.contains("@3")) FileUtil.deleteFileSafe(file);
                report(bar, 75, "Initializing launcher");
                var command = new CommandService(); int rpcPort = NetworkUtil.getAvailablePort(11413);
                command.init(version, Entity.MaxGameMemory, Entity.RoleName, Entity.ServerIp, Entity.ServerPort, Entity.UserId,
                        TokenUtil.generateEncryptToken(userToken), Entity.GameId, working.toString(), skip32.generateRoleUuid(Entity.RoleName, Long.parseLong(Entity.UserId)), socketPort, protocolVersion, true, rpcPort);
                command.useJavaVersion(javaVersion);
                report(bar, 80, "Starting RPC service");
                synchronized (lifecycle) {
                    checkOpen(); Path skins = Path.of(PathUtil.CachePath, "Skins"); Files.createDirectories(skins);
                    rpc = new GameRpcService(rpcPort, Entity.ServerIp, Integer.toString(Entity.ServerPort), Entity.RoleName, Entity.UserId, userToken, version);
                    rpc.connect(skins.toString(), wpf::getSkinListInGame, wpf::getNetGameComponentDownloadList);
                }
                report(bar, 90, "Starting authentication socket service");
                synchronized (lifecycle) {
                    checkOpen(); auth = new AuthLibProtocol(InetAddress.ofLiteral("127.0.0.1"), socketPort, DotNetJson.serialize(mods), Entity.GameVersion, Entity.AccessToken); auth.start();
                }
                report(bar, 95, "Launching game process");
                synchronized (lifecycle) {
                    checkOpen(); gameProcess = command.startGame();
                    gameProcess.onExit().thenRun(() -> { for (var listener : exitListeners) listener.accept(Identifier); });
                }
                report(bar, 100, "Running"); ProgressBar.clearCurrent();
                LOG.log(System.Logger.Level.INFO, "Game process started. Version: " + Entity.GameVersion + ", PID: " + gameProcess.pid() + ", Role: " + Entity.RoleName);
                MemoryOptimizer.getInstance();
            } catch (Exception e) {
                if (!disposed) { var failure = update(100, "Game launch failed"); lastProgress = failure; bar.update(100, failure.Message); progress.accept(failure); }
                synchronized (lifecycle) { closeServices(); }
                LOG.log(System.Logger.Level.ERROR, "Failed to launch game", e); throw e;
            }
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    private void closeServices() { if (auth != null) { auth.close(); auth = null; } if (rpc != null) { rpc.closeControlConnection(); rpc = null; } }
    public CompletableFuture<Void> shutdownAsync() {
        synchronized (lifecycle) {
            disposed = true; closeServices(); Process process = gameProcess;
            if (process != null && process.isAlive()) { process.destroyForcibly(); return process.onExit().thenApply(ignored -> null); }
            return CompletableFuture.completedFuture(null);
        }
    }
    @Override public void close() { shutdownAsync().join(); }
}
