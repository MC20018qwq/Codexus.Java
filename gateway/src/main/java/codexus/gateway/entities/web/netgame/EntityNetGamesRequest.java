package codexus.gateway.entities.web.netgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Gateway/Codexus/Gateway/Entities/Web/NetGame/EntityNetGamesRequest.cs. */
@JsonPropertyOrder({"offset","length"})
public class EntityNetGamesRequest {
    @JsonProperty("offset")
    public int Offset;

    @JsonProperty("length")
    public int Length;

}
