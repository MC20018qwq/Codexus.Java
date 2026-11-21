package codexus.cipher.entities.g79.rentalgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/G79/RentalGame/EntityRentalGameServerAddress.cs. */
@JsonPropertyOrder({"mcserver_host","mcserver_port","user_id"})
public class EntityRentalGameServerAddress {
    @JsonProperty("mcserver_host")
    public String Host;

    @JsonProperty("mcserver_port")
    public Integer Port;

    @JsonProperty("user_id")
    public String UserId = "";

}
