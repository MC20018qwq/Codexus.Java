package codexus.cipher.entities.wpflauncher.rentalgame;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EnumServerType {
    Docker(0),
    Vmware(1),
    DockerGuian(2);

    private final int value;

    EnumServerType(int value) {
        this.value = value;
    }

    @JsonValue
    public int value() {
        return value;
    }

    @JsonCreator
    public static EnumServerType fromValue(long value) {
        for (EnumServerType item : values()) {
            if (item.value == value) {
                return item;
            }
        }
        return null;
    }
}
