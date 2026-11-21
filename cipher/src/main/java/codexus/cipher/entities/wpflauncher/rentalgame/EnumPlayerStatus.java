package codexus.cipher.entities.wpflauncher.rentalgame;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EnumPlayerStatus {
    Permit(0),
    Blacklist(1);

    private final int value;

    EnumPlayerStatus(int value) {
        this.value = value;
    }

    @JsonValue
    public int value() {
        return value;
    }

    @JsonCreator
    public static EnumPlayerStatus fromValue(long value) {
        for (EnumPlayerStatus item : values()) {
            if (item.value == value) {
                return item;
            }
        }
        return null;
    }
}
