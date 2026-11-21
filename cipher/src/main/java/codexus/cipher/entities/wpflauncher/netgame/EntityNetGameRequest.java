package codexus.cipher.entities.wpflauncher.netgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/NetGame/EntityNetGameRequest.cs. */
@JsonPropertyOrder({"available_mc_versions","item_type","length","offset","master_type_id","secondary_type_id"})
public class EntityNetGameRequest {
    @JsonProperty("available_mc_versions")
    @DotNetRequired
    public String[] AvailableMcVersions;

    @JsonProperty("item_type")
    @DotNetRequired
    public int ItemType;

    @JsonProperty("length")
    @DotNetRequired
    public int Length;

    @JsonProperty("offset")
    @DotNetRequired
    public int Offset;

    @JsonProperty("master_type_id")
    @DotNetRequired
    public String MasterTypeId;

    @JsonProperty("secondary_type_id")
    @DotNetRequired
    public String SecondaryTypeId;

}
