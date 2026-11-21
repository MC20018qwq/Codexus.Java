package codexus.gateway.entities.web.javaedition.skins;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Gateway/Codexus/Gateway/Entities/Web/JavaEdition/Skins/EntityBuySkinResultRequest.cs. */
@JsonPropertyOrder({"user_id","orderid","buy_type"})
public class EntityBuySkinResultRequest {
    @JsonProperty("user_id")
    @DotNetRequired
    public String UserId;

    @JsonProperty("orderid")
    @DotNetRequired
    public String OrderId;

    @JsonProperty("buy_type")
    @DotNetRequired
    public int BuyType;

}
