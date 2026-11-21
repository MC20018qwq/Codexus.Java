package codexus.cipher.entities.wpflauncher.netgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/NetGame/EntityQuerySearchByGameResponse.cs. */
@JsonPropertyOrder({"mc_version_id","game_type","iid_list"})
public class EntityQuerySearchByGameResponse {
    @JsonProperty("mc_version_id")
    @DotNetRequired
    public int McVersionId;

    @JsonProperty("game_type")
    @DotNetRequired
    public int GameType;

    @JsonProperty("iid_list")
    @DotNetRequired
    public java.util.List<java.math.BigInteger> IidList;

}
