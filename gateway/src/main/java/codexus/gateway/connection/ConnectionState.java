package codexus.gateway.connection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ConnectionState {
    Establishing(0),
    Connected(1);

    private final int value;

    ConnectionState(int value) {
        this.value = value;
    }

    @JsonValue
    public int value() {
        return value;
    }

    @JsonCreator
    public static ConnectionState fromValue(long value) {
        for (ConnectionState item : values()) {
            if (item.value == value) {
                return item;
            }
        }
        return null;
    }
}
