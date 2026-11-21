package codexus.cipher.entities.wpflauncher.netgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/NetGame/EntityQueryGameCharacters.cs. */
@JsonPropertyOrder({"offset","length","user_id","game_id","game_type"})
public class EntityQueryGameCharacters {
    @JsonProperty("offset")
    public int Offset;

    @JsonProperty("length")
    public int Length = 10;

    @JsonProperty("user_id")
    @DotNetRequired
    public String UserId;

    @JsonProperty("game_id")
    @DotNetRequired
    public String GameId;

    @JsonProperty("game_type")
    public String GameType = "2";

}
