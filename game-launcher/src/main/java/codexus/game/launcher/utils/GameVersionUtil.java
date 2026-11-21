package codexus.game.launcher.utils;

import codexus.cipher.entities.wpflauncher.netgame.EnumGameVersion;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class GameVersionUtil {

    private GameVersionUtil() {
    }

    private static final Map<String, EnumGameVersion> VERSIONS = Map.ofEntries(
            Map.entry("", EnumGameVersion.NONE),
            Map.entry("100.0.0", EnumGameVersion.V_CPP),
            Map.entry("300.0.0", EnumGameVersion.V_X64_CPP),
            Map.entry("200.0.0", EnumGameVersion.V_RTX),
            Map.entry("1.7.10", EnumGameVersion.V_1_7_10),
            Map.entry("1.10.2", EnumGameVersion.V_1_10_2),
            Map.entry("1.8", EnumGameVersion.V_1_8),
            Map.entry("1.11.2", EnumGameVersion.V_1_11_2),
            Map.entry("1.12.2", EnumGameVersion.V_1_12_2),
            Map.entry("1.8.8", EnumGameVersion.V_1_8_8),
            Map.entry("1.8.9", EnumGameVersion.V_1_8_9),
            Map.entry("1.9.4", EnumGameVersion.V_1_9_4),
            Map.entry("1.6.4", EnumGameVersion.V_1_6_4),
            Map.entry("1.7.2", EnumGameVersion.V_1_7_2),
            Map.entry("1.12", EnumGameVersion.V_1_12),
            Map.entry("1.13.2", EnumGameVersion.V_1_13_2),
            Map.entry("1.14.3", EnumGameVersion.V_1_14_3),
            Map.entry("1.15", EnumGameVersion.V_1_15),
            Map.entry("1.16", EnumGameVersion.V_1_16),
            Map.entry("1.18", EnumGameVersion.V_1_18),
            Map.entry("1.19.2", EnumGameVersion.V_1_19_2),
            Map.entry("1.20", EnumGameVersion.V_1_20),
            Map.entry("1.20.6", EnumGameVersion.V_1_20_6),
            Map.entry("1.21", EnumGameVersion.V_1_21),
            Map.entry("1.21.8", EnumGameVersion.V_1_21_8),
            Map.entry("1.21.10", EnumGameVersion.V_1_21_10),
            Map.entry("1.21.11", EnumGameVersion.V_1_21_11)
    );

    private static final Map<EnumGameVersion, String> VERSION_NAMES =
            VERSIONS.entrySet().stream().collect(
                    Collectors.toUnmodifiableMap(
                            Map.Entry::getValue,
                            Map.Entry::getKey
                    )
            );

    public static String getGameVersionFromEnum(EnumGameVersion version) {
        if (version == null) {
            return "";
        }

        String name = VERSION_NAMES.get(version);
        if (name != null) {
            return name;
        }

        return version.name()
                .replace("V_", "")
                .replace("_libs", "")
                .replace("_", ".")
                .strip();
    }

    public static EnumGameVersion getEnumFromGameVersion(String gameVersion) {
        return VERSIONS.getOrDefault(
                Objects.requireNonNull(gameVersion),
                EnumGameVersion.NONE
        );
    }
}