package codexus.cipher.entities.wpflauncher.rentalgame;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EnumServerStatus {
    None(-1),
    ServerOff(0),
    ServerOn(1),
    Uninitialized(2),
    Opening(3),
    Closing(4),
    OutOfDate(5),
    SaveCleaning(6),
    Resetting(7),
    Upgrading(8),
    DiscOverflow(9);

    private final int value;

    EnumServerStatus(int value) {
        this.value = value;
    }

    @JsonValue
    public int value() {
        return value;
    }

    @JsonCreator
    public static EnumServerStatus fromValue(long value) {
        for (EnumServerStatus item : values()) {
            if (item.value == value) {
                return item;
            }
        }
        return null;
    }
}
