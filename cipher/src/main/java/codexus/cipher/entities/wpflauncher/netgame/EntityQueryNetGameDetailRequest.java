package codexus.cipher.entities.wpflauncher.netgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/NetGame/EntityQueryNetGameDetailRequest.cs. */
@JsonPropertyOrder({"item_id"})
public class EntityQueryNetGameDetailRequest {
    @JsonProperty("item_id")
    @DotNetRequired
    public String ItemId;

}
