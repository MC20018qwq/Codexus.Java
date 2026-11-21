package codexus.gateway.entities.web.netgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Gateway/Codexus/Gateway/Entities/Web/NetGame/EntityNetGamesResponse.cs. */
@JsonPropertyOrder({"entities","total"})
public class EntityNetGamesResponse {
    @JsonProperty("entities")
    @DotNetRequired
    public codexus.cipher.entities.wpflauncher.netgame.EntityNetGameItem[] Entities;

    @JsonProperty("total")
    @DotNetRequired
    public int Total;

}
