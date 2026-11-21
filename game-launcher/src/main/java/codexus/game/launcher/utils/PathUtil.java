package codexus.game.launcher.utils;

import java.io.*;
import java.nio.file.*;
import java.util.Locale;
import java.util.regex.Pattern;

public final class PathUtil {
    private PathUtil() { }
    // Launcher data uses the fixed layout under the process working directory.
    public static final String BasePath = Path.of("").toAbsolutePath().normalize().toString();
    public static final String CachePath = Path.of(BasePath, ".game_cache").toString();
    public static final String ResourcePath = Path.of(BasePath, "resources").toString();
    public static final String UpdaterPath = Path.of(BasePath, "updater").toString();
    public static final String ScriptPath = Path.of(UpdaterPath, isWindows() ? "update.bat" : "update.sh").toString();
    public static final String CustomModsPath = Path.of(ResourcePath, "mods").toString();
    public static final String SkinsPath = Path.of(ResourcePath, "skins").toString();
    public static final String WebSitePath = Path.of(ResourcePath, "static").toString();
    public static final String JavaPath = Path.of(CachePath, "Java").toString();
    public static final String Jre8Path = Path.of(JavaPath, "jre8").toString();
    public static final String Jre17Path = Path.of(JavaPath, "jdk17").toString();
    public static final String Jre22Path = Path.of(JavaPath, "jdk22").toString();
    public static final String GamePath = Path.of(CachePath, "Game").toString();
    public static final String GameBasePath = Path.of(GamePath, "Base").toString();
    public static final String GameModsPath = Path.of(CachePath, "GameMods").toString();
    public static final String CppGamePath = Path.of(CachePath, "CppGame").toString();
    private static final Pattern CHINESE = Pattern.compile("[\\u4e00-\\u9fff]");
    private static boolean isWindows() { return System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("windows"); }
    public static void openDirectory(String path) {
        try {
            String command = isWindows() ? "explorer.exe" : System.getProperty("os.name").startsWith("Mac") ? "open" : "xdg-open";
            new ProcessBuilder(command, path).start();
        } catch (IOException e) { System.getLogger(PathUtil.class.getName()).log(System.Logger.Level.ERROR, "Failed to open directory: " + path, e); }
    }
    public static boolean containsChinese(String path) { return CHINESE.matcher(path).find(); }
}
