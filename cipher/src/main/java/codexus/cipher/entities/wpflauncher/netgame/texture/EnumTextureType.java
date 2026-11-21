package codexus.cipher.entities.wpflauncher.netgame.texture;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EnumTextureType {
    SKIN(31),
    SPECIAL_SKIN(42),
    FOUR_DIMENSIONAL_SKIN(41);

    private final int value;

    EnumTextureType(int value) {
        this.value = value;
    }

    @JsonValue
    public int value() {
        return value;
    }

    @JsonCreator
    public static EnumTextureType fromValue(long value) {
        for (EnumTextureType item : values()) {
            if (item.value == value) {
                return item;
            }
        }
        return null;
    }
}
