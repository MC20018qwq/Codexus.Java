package codexus.gateway.entities.web.javaedition.skins;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Gateway/Codexus/Gateway/Entities/Web/JavaEdition/Skins/EntitySkinListResponse.cs. */
@JsonPropertyOrder({"entities","total"})
public class EntitySkinListResponse {
    @JsonProperty("entities")
    @DotNetRequired
    public codexus.cipher.entities.wpflauncher.netgame.skin.EntitySkin[] Entities;

    @JsonProperty("total")
    @DotNetRequired
    public int Total;

}
