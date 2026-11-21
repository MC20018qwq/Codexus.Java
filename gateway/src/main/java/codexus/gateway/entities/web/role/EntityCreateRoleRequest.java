package codexus.gateway.entities.web.role;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Gateway/Codexus/Gateway/Entities/Web/Role/EntityCreateRoleRequest.cs. */
@JsonPropertyOrder({"id","name","game","type"})
public class EntityCreateRoleRequest {
    @JsonProperty("id")
    @DotNetRequired
    public String UserId;

    @JsonProperty("name")
    @DotNetRequired
    public String RoleName;

    @JsonProperty("game")
    @DotNetRequired
    public String GameId;

    @JsonProperty("type")
    @DotNetRequired
    public String GameType;

}
