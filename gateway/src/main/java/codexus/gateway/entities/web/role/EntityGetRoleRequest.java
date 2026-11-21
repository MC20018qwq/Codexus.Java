package codexus.gateway.entities.web.role;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Gateway/Codexus/Gateway/Entities/Web/Role/EntityGetRoleRequest.cs. */
@JsonPropertyOrder({"id","game","type"})
public class EntityGetRoleRequest {
    @JsonProperty("id")
    public String UserId = "";

    @JsonProperty("game")
    public String GameId = "";

    @JsonProperty("type")
    public String GameType = "";

}
