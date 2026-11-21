package codexus.cipher.entities.wpflauncher.netgame.skin;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/NetGame/Skin/EntitySkinDetailsRequest.cs. */
@JsonPropertyOrder({"channel_id","entity_ids","is_has","with_price","with_title_image"})
public class EntitySkinDetailsRequest {
    @JsonProperty("channel_id")
    @DotNetRequired
    public String ChannelId;

    @JsonProperty("entity_ids")
    @DotNetRequired
    public java.util.List<String> EntityIds;

    @JsonProperty("is_has")
    @DotNetRequired
    public int IsHas;

    @JsonProperty("with_price")
    @DotNetRequired
    public int WithPrice;

    @JsonProperty("with_title_image")
    @DotNetRequired
    public int WithTitleImage;

}
