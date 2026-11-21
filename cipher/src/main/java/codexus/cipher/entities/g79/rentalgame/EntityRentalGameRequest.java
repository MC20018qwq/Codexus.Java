package codexus.cipher.entities.g79.rentalgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/G79/RentalGame/EntityRentalGameRequest.cs. */
@JsonPropertyOrder({"sort_type","order_type","offset"})
public class EntityRentalGameRequest {
    @JsonProperty("sort_type")
    @DotNetRequired
    public int SortType;

    @JsonProperty("order_type")
    @DotNetRequired
    public int OrderType;

    @JsonProperty("offset")
    @DotNetRequired
    public int Offset;

}
