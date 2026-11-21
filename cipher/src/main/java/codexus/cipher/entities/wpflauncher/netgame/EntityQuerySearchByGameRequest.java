package codexus.cipher.entities.wpflauncher.netgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/NetGame/EntityQuerySearchByGameRequest.cs. */
@JsonPropertyOrder({"mc_version_id","game_type"})
public class EntityQuerySearchByGameRequest {
    @JsonProperty("mc_version_id")
    @DotNetRequired
    public int McVersionId;

    @JsonProperty("game_type")
    @DotNetRequired
    public int GameType;

}
