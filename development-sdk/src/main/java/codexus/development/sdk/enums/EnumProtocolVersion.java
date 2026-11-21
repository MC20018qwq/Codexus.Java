package codexus.development.sdk.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EnumProtocolVersion {
    None(-1),
    V1076(5),
    V108X(47),
    V1122(340),
    V1165(754),
    V1180(757),
    V1200(763),
    V1206(766),
    V1210(767),
    V1218(772),
    V12110(773),
    V12111(774);

    private final int value;

    EnumProtocolVersion(int value) {
        this.value = value;
    }

    @JsonValue
    public int value() {
        return value;
    }

    @JsonCreator
    public static EnumProtocolVersion fromValue(long value) {
        for (EnumProtocolVersion item : values()) {
            if (item.value == value) {
                return item;
            }
        }
        return null;
    }
}
