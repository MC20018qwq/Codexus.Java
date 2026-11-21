package codexus.cipher.entities.wpflauncher.netgame.texture;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EnumGType {
    None(-1),
    SingleGame(1),
    NetGame(2),
    McGame(7),
    ServerGame(8),
    LanGame(9),
    OnlineLobbyGame(10);

    private final int value;

    EnumGType(int value) {
        this.value = value;
    }

    @JsonValue
    public int value() {
        return value;
    }

    @JsonCreator
    public static EnumGType fromValue(long value) {
        for (EnumGType item : values()) {
            if (item.value == value) {
                return item;
            }
        }
        return null;
    }
}
