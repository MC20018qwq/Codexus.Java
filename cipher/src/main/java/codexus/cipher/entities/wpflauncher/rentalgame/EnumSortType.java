package codexus.cipher.entities.wpflauncher.rentalgame;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EnumSortType {
    General(0),
    OnlineCount(1),
    Time(2),
    Like(3);

    private final int value;

    EnumSortType(int value) {
        this.value = value;
    }

    @JsonValue
    public int value() {
        return value;
    }

    @JsonCreator
    public static EnumSortType fromValue(long value) {
        for (EnumSortType item : values()) {
            if (item.value == value) {
                return item;
            }
        }
        return null;
    }
}
