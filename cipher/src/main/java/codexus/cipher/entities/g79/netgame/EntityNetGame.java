package codexus.cipher.entities.g79.netgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/G79/NetGame/EntityNetGame.cs. */
@JsonPropertyOrder({"res","tag","campaign_id"})
public class EntityNetGame {
    @JsonProperty("res")
    @DotNetRequired
    public java.util.List<codexus.cipher.entities.g79.netgame.EntityNetGameItem> Res;

    @JsonProperty("tag")
    @DotNetRequired
    public int Tag;

    @JsonProperty("campaign_id")
    @DotNetRequired
    public int CampaignId;

}
