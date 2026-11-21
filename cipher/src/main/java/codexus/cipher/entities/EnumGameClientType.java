package codexus.cipher.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EnumGameClientType {
    All(0),
    Java(1),
    Cpp(2);

    private final int value;

    EnumGameClientType(int value) {
        this.value = value;
    }

    @JsonValue
    public int value() {
        return value;
    }

    @JsonCreator
    public static EnumGameClientType fromValue(long value) {
        for (EnumGameClientType item : values()) {
            if (item.value == value) {
                return item;
            }
        }
        return null;
    }
}
