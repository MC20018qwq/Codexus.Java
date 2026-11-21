package codexus.cipher.entities.wpflauncher.netgame.texture;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/NetGame/Texture/EntityUserGameTextureRequest.cs. */
@JsonPropertyOrder({"user_id","game_type","client_type"})
public class EntityUserGameTextureRequest {
    @JsonProperty("user_id")
    public String UserId = "";

    @JsonProperty("game_type")
    public String GameType = "";

    @JsonProperty("client_type")
    public int ClientType = 0;

    /** Known enum value, or null when the raw code is undeclared. */
    public codexus.cipher.entities.EnumGameClientType getClientType() {
        return codexus.cipher.entities.EnumGameClientType.fromValue(ClientType);
    }

    public void setClientType(codexus.cipher.entities.EnumGameClientType value) {
        ClientType = java.util.Objects.requireNonNull(value).value();
    }

}
