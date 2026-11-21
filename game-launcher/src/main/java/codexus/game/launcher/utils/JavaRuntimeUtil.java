package codexus.game.launcher.utils;

import codexus.cipher.entities.wpflauncher.netgame.EnumGameVersion;
import java.nio.file.Path;
import java.util.Objects;

/** Runtime policy from NetEase ks.l and GameM.et, rather than vanilla Minecraft's minimum Java. */
public final class JavaRuntimeUtil {
    private JavaRuntimeUtil() { }

    public static int baseline(EnumGameVersion version) {
        Objects.requireNonNull(version, "gameVersion");
        if (version == EnumGameVersion.NONE || version.value() >= EnumGameVersion.V_CPP.value())
            throw new IllegalArgumentException("Not a Java game version: " + version);
        if (version.value() >= EnumGameVersion.V_1_20_6.value()) return 22;
        if (version.value() >= EnumGameVersion.V_1_18.value()) return 17;
        return 8;
    }

    public static int requiredVersion(EnumGameVersion version, int componentRequirement, boolean isRental) {
        int required = Math.max(baseline(version), isRental ? 0 : componentRequirement);
        directoryName(required); // Fail explicitly if the server asks for an unsupported runtime.
        return required;
    }

    public static String directoryName(int major) {
        return switch (major) {
            case 8 -> "jre8";
            case 17 -> "jdk17";
            case 22 -> "jdk22";
            default -> throw new IllegalArgumentException("Unsupported NetEase Java runtime: " + major);
        };
    }

    public static Path executable(int major) { return executable(Path.of(PathUtil.JavaPath), major); }
    public static Path executable(Path root, int major) { return root.resolve(directoryName(major)).resolve("bin/javaw.exe"); }

    public static String archiveName(int major, boolean is64Bit) {
        directoryName(major);
        if (!is64Bit && major != 8) throw new IllegalArgumentException("Java " + major + " requires a 64-bit runtime");
        return "jdk" + major + "_v" + (is64Bit ? 64 : 32) + "_v1.7z";
    }
}
