package codexus.gateway.entities.web.javaedition.skins;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Gateway/Codexus/Gateway/Entities/Web/JavaEdition/Skins/EntitySkinListRequest.cs. */
@JsonPropertyOrder({"offset","length"})
public class EntitySkinListRequest {
    @JsonProperty("offset")
    public int Offset;

    @JsonProperty("length")
    public int Length;

}
