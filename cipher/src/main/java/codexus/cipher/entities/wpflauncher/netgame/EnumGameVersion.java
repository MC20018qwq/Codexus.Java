package codexus.cipher.entities.wpflauncher.netgame;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EnumGameVersion {

    NONE(0, ""),
    V_CPP(100000000, "100.0.0"),
    V_X64_CPP(300000000, "300.0.0"),
    V_RTX(200000000, "200.0.0"),
    V_1_7_10(1007010, "1.7.10"),
    V_1_10_2(1010002, "1.10.2"),
    V_1_8(1008000, "1.8"),
    V_1_11_2(1011002, "1.11.2"),
    V_1_12_2(1012002, "1.12.2"),
    V_1_8_8(1008008, "1.8.8"),
    V_1_8_9(1008009, "1.8.9"),
    V_1_9_4(1009004, "1.9.4"),
    V_1_6_4(1006004, "1.6.4"),
    V_1_7_2(1007002, "1.7.2"),
    V_1_12(1012000, "1.12"),
    V_1_13_2(1013002, "1.13.2"),
    V_1_14_3(1014003, "1.14.3"),
    V_1_15(1015000, "1.15"),
    V_1_16(1016000, "1.16"),
    V_1_18(1018000, "1.18"),
    V_1_19_2(1019002, "1.19.2"),
    V_1_20(1020000, "1.20"),
    V_1_20_6(1020006, "1.20.6"),
    V_1_21(1021000, "1.21"),
    V_1_21_8(1021008, "1.21.8"),
    V_1_21_10(1021010, "1.21.10"),
    V_1_21_11(1021011, "1.21.11");

    private final long value;
    private final String xmlName;

    EnumGameVersion(long value, String xmlName) {
        this.value = value;
        this.xmlName = xmlName;
    }

    @JsonValue
    public long value() {
        return value;
    }

    public String xmlName() {
        return xmlName;
    }

    @JsonCreator
    public static EnumGameVersion fromValue(long value) {
        for (EnumGameVersion item : values()) {
            if (item.value == value) {
                return item;
            }
        }
        return null;
    }
}