package codexus.heypixel.plugin.utilities;

import java.nio.file.Path;
public final class LauncherGamePaths {
    private LauncherGamePaths(){}
    public static final Path CacheRoot=Path.of(".game_cache").toAbsolutePath().normalize();
    public static final Path GameDirectory=CacheRoot.resolve("Game"),GameModsDirectory=CacheRoot.resolve("GameMods");
}
