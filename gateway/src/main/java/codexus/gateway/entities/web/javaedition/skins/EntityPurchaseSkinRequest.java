package codexus.gateway.entities.web.javaedition.skins;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Gateway/Codexus/Gateway/Entities/Web/JavaEdition/Skins/EntityPurchaseSkinRequest.cs. */
@JsonPropertyOrder({"user_id","item_id"})
public class EntityPurchaseSkinRequest {
    @JsonProperty("user_id")
    @DotNetRequired
    public String UserId;

    @JsonProperty("item_id")
    @DotNetRequired
    public String ItemId;

}
