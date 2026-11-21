package codexus.gateway.entities.web.rentalgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Gateway/Codexus/Gateway/Entities/Web/RentalGame/EntityRentalGameResponse.cs. */
@JsonPropertyOrder({"entities","total"})
public class EntityRentalGameResponse {
    @JsonProperty("entities")
    @DotNetRequired
    public codexus.cipher.entities.wpflauncher.rentalgame.EntityRentalGame[] Entities;

    @JsonProperty("total")
    @DotNetRequired
    public int Total;

}
