package codexus.cipher.entities.wpflauncher.netgame.skin;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/NetGame/Skin/EntityQuerySkinByNameRequest.cs. */
@JsonPropertyOrder({"is_has","is_sync","item_type","keyword","length","master_type_id","offset","price_type","secondary_type_id","sort_type","year"})
public class EntityQuerySkinByNameRequest {
    @JsonProperty("is_has")
    @DotNetRequired
    public boolean IsHas;

    @JsonProperty("is_sync")
    @DotNetRequired
    public int IsSync;

    @JsonProperty("item_type")
    @DotNetRequired
    public int ItemType;

    @JsonProperty("keyword")
    @DotNetRequired
    public String Keyword;

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
    public String SecondaryTypeId;

    @JsonProperty("sort_type")
    @DotNetRequired
    public int SortType;

    @JsonProperty("year")
    @DotNetRequired
    public int Year;

}
