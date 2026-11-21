package codexus.gateway.manager;

import codexus.gateway.entities.web.gateway.*;
import codexus.game.launcher.services.java.*;
import codexus.interceptors.Interceptor;
import java.util.*;
import java.util.concurrent.*;

public final class GameManager implements AutoCloseable {
    public static final GameManager Instance = new GameManager();
    public final ConcurrentMap<UUID, Interceptor> Interceptors = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, LauncherService> launchers = new ConcurrentHashMap<>();
    public List<EntityQueryInterceptors> getQueryInterceptors() {
        var result = new ArrayList<EntityQueryInterceptors>();
        for (var interceptor : Interceptors.values()) {
            var value = new EntityQueryInterceptors(); value.Id = Integer.toString(result.size()); value.GameType = "Java";
            value.IsRental = interceptor.isRental(); value.Name = interceptor.Identifier; value.Address = interceptor.getForwardAddress() + ":" + interceptor.getForwardPort();
            value.Role = interceptor.getNickName(); value.Server = interceptor.getServerName(); value.Version = interceptor.getServerVersion();
            value.LocalAddress = interceptor.getLocalAddress() + ":" + interceptor.getLocalPort(); result.add(value);
        }
        return result;
    }
    public List<EntityQueryLaunchers> getQueryLaunchers() {
        var result = new ArrayList<EntityQueryLaunchers>();
        for (var launcher : launchers.values()) {
            var value = new EntityQueryLaunchers(); value.Id = Integer.toString(result.size()); value.Name = launcher.Identifier;
            value.Role = launcher.Entity.RoleName; value.Server = launcher.Entity.GameName; value.Version = launcher.Entity.GameVersion;
            value.StatusMessage = launcher.getLastProgress().Message; value.Progress = launcher.getLastProgress().Percent;
            value.ProcessId = launcher.getProcess() == null ? -1 : Math.toIntExact(launcher.getProcess().pid()); result.add(value);
        }
        return result;
    }
    public void registerActions() {
        RepairService.registerKillGameAction(() -> launchers.values().forEach(launcher -> {
            Process process = launcher.getProcess(); if (process != null && process.isAlive()) process.destroyForcibly();
        }));
    }
    public void addInterceptor(Interceptor interceptor) {
        if (Interceptors.putIfAbsent(interceptor.Identifier, interceptor) != null) throw new IllegalArgumentException("Duplicate interceptor");
    }
    public void shutdownInterceptor(UUID id) { var interceptor = Interceptors.remove(id); if (interceptor != null) interceptor.shutdownAsync(); }
    public void addLauncher(LauncherService launcher) {
        if (launchers.putIfAbsent(launcher.Identifier, launcher) != null) throw new IllegalArgumentException("Duplicate launcher");
        launcher.addExitedListener(this::shutdownLauncher);
        Process process = launcher.getProcess();
        if (process != null && !process.isAlive()) shutdownLauncher(launcher.Identifier);
        launcher.getLaunchTask().whenComplete((value, error) -> { if (error != null) shutdownLauncher(launcher.Identifier); });
    }
    public void shutdownLauncher(UUID id) { var launcher = launchers.remove(id); if (launcher != null) launcher.shutdownAsync().join(); }
    @Override public void close() {
        for (UUID id : List.copyOf(Interceptors.keySet())) shutdownInterceptor(id);
        for (UUID id : List.copyOf(launchers.keySet())) shutdownLauncher(id);
    }
}
