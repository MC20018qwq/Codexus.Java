package codexus.gateway.entities.server;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Gateway/Codexus/Gateway/Entities/Server/EntityVersionRequest.cs. */
@JsonPropertyOrder({"version"})
public class EntityVersionRequest {
    @JsonProperty("version")
    @DotNetRequired
    public String Version;

}
