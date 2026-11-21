package codexus.cipher.entities.wpflauncher.netgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/NetGame/EntityNetGameKeyword.cs. */
@JsonPropertyOrder({"is_has","is_sync","item_type","keyword","length","master_type_id","network_tag","offset","order","secondary_type_id","sort_type","year"})
public class EntityNetGameKeyword {
    @JsonProperty("is_has")
    public boolean IsHas;

    @JsonProperty("is_sync")
    public int IsSync;

    @JsonProperty("item_type")
    public int ItemType = 1;

    @JsonProperty("keyword")
    @DotNetRequired
    public String Keyword;

    @JsonProperty("length")
    public int Length = 24;

    @JsonProperty("master_type_id")
    public String MasterTypeId = "2";

    @JsonProperty("network_tag")
    public boolean NetworkTag;

    @JsonProperty("offset")
    public int Offset;

    @JsonProperty("order")
    public int Order;

    @JsonProperty("secondary_type_id")
    public String SecondaryTypeId = "";

    @JsonProperty("sort_type")
    public int SortType = 2;

    @JsonProperty("year")
    public int Year;

}
