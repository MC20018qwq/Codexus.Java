package codexus.development.sdk.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EnumLoaderType {
    RakNet(0);

    private final int value;

    EnumLoaderType(int value) {
        this.value = value;
    }

    @JsonValue
    public int value() {
        return value;
    }

    @JsonCreator
    public static EnumLoaderType fromValue(long value) {
        for (EnumLoaderType item : values()) {
            if (item.value == value) {
                return item;
            }
        }
        return null;
    }
}
