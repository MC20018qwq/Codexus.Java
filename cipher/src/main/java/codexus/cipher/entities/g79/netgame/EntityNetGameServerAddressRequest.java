package codexus.cipher.entities.g79.netgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/G79/NetGame/EntityNetGameServerAddressRequest.cs. */
@JsonPropertyOrder({"item_id"})
public class EntityNetGameServerAddressRequest {
    @JsonProperty("item_id")
    @DotNetRequired
    public String ItemId;

}
