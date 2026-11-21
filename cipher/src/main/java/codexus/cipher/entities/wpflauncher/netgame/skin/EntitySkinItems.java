package codexus.cipher.entities.wpflauncher.netgame.skin;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/NetGame/Skin/EntitySkinItems.cs. */
@JsonPropertyOrder({"item_ids"})
public class EntitySkinItems {
    @JsonProperty("item_ids")
    public String[] ItemIds = new String[0];

}
