package codexus.cipher.entities.wpflauncher.netgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/NetGame/EntityQueryNetGameRequest.cs. */
@JsonPropertyOrder({"channel_id","entity_ids","is_has","with_price","with_title_image"})
public class EntityQueryNetGameRequest {
    @JsonProperty("channel_id")
    public String ChannelId = "21";

    @JsonProperty("entity_ids")
    @DotNetRequired
    public String[] EntityIds;

    @JsonProperty("is_has")
    public boolean IsHas;

    @JsonProperty("with_price")
    public int WithPrice;

    @JsonProperty("with_title_image")
    public int WithTitleImage = 1;

}
