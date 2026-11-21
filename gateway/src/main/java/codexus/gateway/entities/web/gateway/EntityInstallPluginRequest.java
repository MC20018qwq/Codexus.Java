package codexus.gateway.entities.web.gateway;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Gateway/Codexus/Gateway/Entities/Web/Gateway/EntityInstallPluginRequest.cs. */
@JsonPropertyOrder({"id","plugin"})
public class EntityInstallPluginRequest {
    @JsonProperty("id")
    @DotNetRequired
    public String Id;

    @JsonProperty("plugin")
    @DotNetRequired
    public codexus.gateway.entities.web.gateway.EntityInstallPlugin Plugin;

}
