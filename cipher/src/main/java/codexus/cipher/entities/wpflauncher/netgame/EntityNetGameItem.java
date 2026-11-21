package codexus.cipher.entities.wpflauncher.netgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/NetGame/EntityNetGameItem.cs. */
@JsonPropertyOrder({"entity_id","brief_summary","name","online_count","title_image_url"})
public class EntityNetGameItem {
    @JsonProperty("entity_id")
    public String EntityId = "";

    @JsonProperty("brief_summary")
    public String BriefSummary = "";

    @JsonProperty("name")
    public String Name = "";

    @JsonProperty("online_count")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = codexus.cipher.entities.converter.NetEaseIntConverter.class)
    public String OnlineCount = "";

    @JsonProperty("title_image_url")
    public String TitleImageUrl = "";

}
