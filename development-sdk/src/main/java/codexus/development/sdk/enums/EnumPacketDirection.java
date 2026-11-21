package codexus.development.sdk.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EnumPacketDirection {
    ServerBound(0),
    ClientBound(1);

    private final int value;

    EnumPacketDirection(int value) {
        this.value = value;
    }

    @JsonValue
    public int value() {
        return value;
    }

    @JsonCreator
    public static EnumPacketDirection fromValue(long value) {
        for (EnumPacketDirection item : values()) {
            if (item.value == value) {
                return item;
            }
        }
        return null;
    }
}
