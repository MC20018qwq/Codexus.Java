package codexus.cipher.entities.g79.rentalgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/G79/RentalGame/EntityRentalGameServerAddressRequest.cs. */
@JsonPropertyOrder({"server_id","pwd"})
public class EntityRentalGameServerAddressRequest {
    @JsonProperty("server_id")
    @DotNetRequired
    public String ServerId;

    @JsonProperty("pwd")
    @DotNetRequired
    public String Password;

}
