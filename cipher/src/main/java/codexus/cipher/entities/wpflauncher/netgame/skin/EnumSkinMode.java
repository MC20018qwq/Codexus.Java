package codexus.cipher.entities.wpflauncher.netgame.skin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EnumSkinMode {
    Default(0),
    Slim(1);

    private final int value;

    EnumSkinMode(int value) {
        this.value = value;
    }

    @JsonValue
    public int value() {
        return value;
    }

    @JsonCreator
    public static EnumSkinMode fromValue(long value) {
        for (EnumSkinMode item : values()) {
            if (item.value == value) {
                return item;
            }
        }
        return null;
    }
}
