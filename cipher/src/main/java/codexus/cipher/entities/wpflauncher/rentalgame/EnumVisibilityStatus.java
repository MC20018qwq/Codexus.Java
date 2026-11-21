package codexus.cipher.entities.wpflauncher.rentalgame;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EnumVisibilityStatus {
    Public(0),
    Friend(1),
    Private(2),
    Password(3);

    private final int value;

    EnumVisibilityStatus(int value) {
        this.value = value;
    }

    @JsonValue
    public int value() {
        return value;
    }

    @JsonCreator
    public static EnumVisibilityStatus fromValue(long value) {
        for (EnumVisibilityStatus item : values()) {
            if (item.value == value) {
                return item;
            }
        }
        return null;
    }
}
