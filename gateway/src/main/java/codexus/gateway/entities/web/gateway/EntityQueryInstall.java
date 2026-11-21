package codexus.gateway.entities.web.gateway;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Gateway/Codexus/Gateway/Entities/Web/Gateway/EntityQueryInstall.cs. */
@JsonPropertyOrder({"id","version","status","installed","restart"})
public class EntityQueryInstall {
    @JsonProperty("id")
    public String PluginId = "";

    @JsonProperty("version")
    public String PluginVersion = "";

    @JsonProperty("status")
    public boolean PluginIsInstalled;

    @JsonProperty("installed")
    public String PluginInstalledVersion = "";

    @JsonProperty("restart")
    public boolean PluginIsWaitingRestart;

}
