package codexus.game.launcher.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EnumLaunchType {
    Custom(0),
    Official(1);

    private final int value;

    EnumLaunchType(int value) {
        this.value = value;
    }

    @JsonValue
    public int value() {
        return value;
    }

    @JsonCreator
    public static EnumLaunchType fromValue(long value) {
        for (EnumLaunchType item : values()) {
            if (item.value == value) {
                return item;
            }
        }
        return null;
    }
}
