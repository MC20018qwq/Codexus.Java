package codexus.gateway.entities.web.gateway;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Gateway/Codexus/Gateway/Entities/Web/Gateway/EntityReselectServer.cs. */
@JsonPropertyOrder({"id","game_version_id","game_version","game_name","game_id","address","port"})
public class EntityReselectServer {
    @JsonProperty("id")
    public String Id = "";

    @JsonProperty("game_version_id")
    public int GameVersionId;

    @JsonProperty("game_version")
    public String GameVersion = "";

    @JsonProperty("game_name")
    public String GameName = "";

    @JsonProperty("game_id")
    public String GameId = "";

    @JsonProperty("address")
    public String Address = "";

    @JsonProperty("port")
    public int Port;

}
