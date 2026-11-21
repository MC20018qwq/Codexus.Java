package codexus.cipher.entities.wpflauncher.netgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/NetGame/EntityBuyItemResult.cs. */
@JsonPropertyOrder({"orderid","buy_type"})
public class EntityBuyItemResult {
    @JsonProperty("orderid")
    public String OrderId = "";

    @JsonProperty("buy_type")
    public int BuyType;

}
