package codexus.cipher.entities.wpflauncher.netgame.skin;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/NetGame/Skin/EntityPurchaseResult.cs. */
@JsonPropertyOrder({"buy_type","entity_id"})
public class EntityPurchaseResult {
    @JsonProperty("buy_type")
    public int BuyType;

    @JsonProperty("entity_id")
    public String EntityId = "";

}
