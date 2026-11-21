package codexus.development.sdk.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EnumConnectionState {
    Handshaking(0),
    Status(1),
    Login(2),
    Configuration(3),
    Play(4);

    private final int value;

    EnumConnectionState(int value) {
        this.value = value;
    }

    @JsonValue
    public int value() {
        return value;
    }

    @JsonCreator
    public static EnumConnectionState fromValue(long value) {
        for (EnumConnectionState item : values()) {
            if (item.value == value) {
                return item;
            }
        }
        return null;
    }
}
