package codexus.cipher.entities.wpflauncher.netgame.skin;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/NetGame/Skin/EntityFreeSkinListRequest.cs. */
@JsonPropertyOrder({"is_has","item_type","length","master_type_id","offset","price_type","secondary_type_id"})
public class EntityFreeSkinListRequest {
    @JsonProperty("is_has")
    @DotNetRequired
    public boolean IsHas;

    @JsonProperty("item_type")
    @DotNetRequired
    public int ItemType;

    @JsonProperty("length")
    @DotNetRequired
    public int Length;

    @JsonProperty("master_type_id")
    @DotNetRequired
    public int MasterTypeId;

    @JsonProperty("offset")
    @DotNetRequired
    public int Offset;

    @JsonProperty("price_type")
    @DotNetRequired
    public int PriceType;

    @JsonProperty("secondary_type_id")
    @DotNetRequired
    public int SecondaryTypeId;

}
