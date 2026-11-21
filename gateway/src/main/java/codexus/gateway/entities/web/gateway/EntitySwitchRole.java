package codexus.gateway.entities.web.gateway;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Gateway/Codexus/Gateway/Entities/Web/Gateway/EntitySwitchRole.cs. */
@JsonPropertyOrder({"id","user_id","role"})
public class EntitySwitchRole {
    @JsonProperty("id")
    @DotNetRequired
    public String Id;

    @JsonProperty("user_id")
    @DotNetRequired
    public String UserId;

    @JsonProperty("role")
    @DotNetRequired
    public String RoleName;

}
