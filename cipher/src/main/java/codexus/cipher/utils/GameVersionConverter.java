package codexus.cipher.utils;

import codexus.cipher.entities.wpflauncher.netgame.EnumGameVersion;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class GameVersionConverter {

    private static final Map<String, EnumGameVersion> VERSION_MAP =
            new LinkedHashMap<>();

    private static final Map<EnumGameVersion, Integer> REVERSE_VERSION_MAP =
            new EnumMap<>(EnumGameVersion.class);

    static {
        VERSION_MAP.put("", EnumGameVersion.NONE);
        VERSION_MAP.put("1", EnumGameVersion.V_1_7_10);
        VERSION_MAP.put("2", EnumGameVersion.V_1_8);
        VERSION_MAP.put("3", EnumGameVersion.V_1_9_4);
        VERSION_MAP.put("5", EnumGameVersion.V_1_11_2);
        VERSION_MAP.put("6", EnumGameVersion.V_1_8_8);
        VERSION_MAP.put("7", EnumGameVersion.V_1_10_2);
        VERSION_MAP.put("8", EnumGameVersion.V_1_6_4);
        VERSION_MAP.put("9", EnumGameVersion.V_1_7_2);
        VERSION_MAP.put("10", EnumGameVersion.V_1_12_2);
        VERSION_MAP.put("11", EnumGameVersion.NONE);
        VERSION_MAP.put("12", EnumGameVersion.V_1_8_9);
        VERSION_MAP.put("13", EnumGameVersion.V_CPP);
        VERSION_MAP.put("14", EnumGameVersion.V_1_13_2);
        VERSION_MAP.put("15", EnumGameVersion.V_1_14_3);
        VERSION_MAP.put("16", EnumGameVersion.V_1_15);
        VERSION_MAP.put("17", EnumGameVersion.V_1_16);
        VERSION_MAP.put("18", EnumGameVersion.V_RTX);
        VERSION_MAP.put("19", EnumGameVersion.V_1_18);
        VERSION_MAP.put("23", EnumGameVersion.V_1_19_2);
        VERSION_MAP.put("21", EnumGameVersion.V_1_20);
        VERSION_MAP.put("22", EnumGameVersion.V_1_20_6);
        VERSION_MAP.put("24", EnumGameVersion.V_1_21);
        VERSION_MAP.put("25", EnumGameVersion.V_1_21_8);
        VERSION_MAP.put("26", EnumGameVersion.V_1_21_10);
        VERSION_MAP.put("27", EnumGameVersion.V_1_21_11);

        VERSION_MAP.forEach((id, version) -> {
            // NetEase aie.d excludes the empty key: NONE has the explicit ID 11.
            if (!id.isEmpty()) REVERSE_VERSION_MAP.put(version, Integer.parseInt(id));
        });
    }

    private GameVersionConverter() {
    }

    public static EnumGameVersion convert(int id) {
        return VERSION_MAP.getOrDefault(
                Integer.toString(id),
                EnumGameVersion.NONE
        );
    }

    public static int convert(EnumGameVersion version) {
        if (version == null) {
            return -1;
        }

        // NetEase aie.h uses the regular Bedrock ID when serializing RTX.
        if (version == EnumGameVersion.V_RTX) version = EnumGameVersion.V_CPP;
        return REVERSE_VERSION_MAP.getOrDefault(version, -1);
    }
}