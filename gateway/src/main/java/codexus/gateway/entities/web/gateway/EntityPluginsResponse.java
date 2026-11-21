package codexus.gateway.entities.web.gateway;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Gateway/Codexus/Gateway/Entities/Web/Gateway/EntityPluginsResponse.cs. */
@JsonPropertyOrder({"id","name","description","version","author","status","waiting_restart"})
public class EntityPluginsResponse {
    @JsonProperty("id")
    @DotNetRequired
    public String PluginId;

    @JsonProperty("name")
    @DotNetRequired
    public String PluginName;

    @JsonProperty("description")
    @DotNetRequired
    public String PluginDescription;

    @JsonProperty("version")
    @DotNetRequired
    public String PluginVersion;

    @JsonProperty("author")
    @DotNetRequired
    public String PluginAuthor;

    @JsonProperty("status")
    @DotNetRequired
    public String PluginStatus;

    @JsonProperty("waiting_restart")
    @DotNetRequired
    public boolean PluginWaitingRestart;

}
