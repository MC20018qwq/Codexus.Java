package codexus.gateway.entities.web.rentalgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Gateway/Codexus/Gateway/Entities/Web/RentalGame/EntityRentalGameRequest.cs. */
@JsonPropertyOrder({"offset"})
public class EntityRentalGameRequest {
    @JsonProperty("offset")
    public int Offset;

}
