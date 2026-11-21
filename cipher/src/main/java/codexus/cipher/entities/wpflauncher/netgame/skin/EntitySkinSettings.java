package codexus.cipher.entities.wpflauncher.netgame.skin;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/NetGame/Skin/EntitySkinSettings.cs. */
@JsonPropertyOrder({"client_type","game_type","skin_id","skin_mode","skin_type"})
public class EntitySkinSettings {
    @JsonProperty("client_type")
    @DotNetRequired
    public String ClientType;

    @JsonProperty("game_type")
    @DotNetRequired
    public int GameType;

    @JsonProperty("skin_id")
    @DotNetRequired
    public String SkinId;

    @JsonProperty("skin_mode")
    @DotNetRequired
    public int SkinMode;

    @JsonProperty("skin_type")
    @DotNetRequired
    public int SkinType;

}
