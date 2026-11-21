package codexus.gateway.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Platform {
    Desktop(0),
    Mobile(1),
    Mixed(2);

    private final int value;

    Platform(int value) {
        this.value = value;
    }

    @JsonValue
    public int value() {
        return value;
    }

    @JsonCreator
    public static Platform fromValue(long value) {
        for (Platform item : values()) {
            if (item.value == value) {
                return item;
            }
        }
        return null;
    }
}
