package codexus.game.launcher.services.java;

import codexus.game.launcher.utils.*;
import java.nio.file.Path;
import java.util.Locale;

public final class RepairService {
    private static volatile Runnable killGameAction;
    private RepairService() { }
    public static void registerKillGameAction(Runnable action) { killGameAction = action; }
    public static void clearClientResources() {
        Runnable action = killGameAction;
        if (action != null) action.run();
        else try (var processes = ProcessHandle.allProcesses()) {
            processes.filter(process -> process.info().command().map(command -> Path.of(command).getFileName().toString().equalsIgnoreCase("javaw.exe")).orElse(false))
                    .filter(process -> process.info().commandLine().map(command -> command.toLowerCase(Locale.ROOT).contains(PathUtil.CachePath.toLowerCase(Locale.ROOT))).orElse(false))
                    .forEach(process -> { try (var children = process.descendants()) { children.forEach(ProcessHandle::destroyForcibly); } process.destroyForcibly(); });
        }
        if (!FileUtil.deleteDirectorySafe(PathUtil.CachePath)) throw new IllegalStateException("Failed to delete game cache: " + PathUtil.CachePath);
    }
}
