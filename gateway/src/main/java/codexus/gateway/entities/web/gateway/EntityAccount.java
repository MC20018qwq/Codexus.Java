package codexus.gateway.entities.web.gateway;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Gateway/Codexus/Gateway/Entities/Web/Gateway/EntityAccount.cs. */
@JsonPropertyOrder({"id","alias"})
public class EntityAccount {
    @JsonProperty("id")
    @DotNetRequired
    public String UserId;

    @JsonProperty("alias")
    @DotNetRequired
    public String Alias;

}
