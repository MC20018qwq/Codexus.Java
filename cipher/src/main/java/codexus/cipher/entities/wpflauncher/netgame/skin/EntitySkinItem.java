package codexus.cipher.entities.wpflauncher.netgame.skin;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/NetGame/Skin/EntitySkinItem.cs. */
@JsonPropertyOrder({"item_id"})
public class EntitySkinItem {
    @JsonProperty("item_id")
    public String ItemId = "";

}
