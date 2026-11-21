package codexus.cipher.entities.wpflauncher.rentalgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/RentalGame/EntityQueryRentalGameServerAddress.cs. */
@JsonPropertyOrder({"server_id","pwd"})
public class EntityQueryRentalGameServerAddress {
    @JsonProperty("server_id")
    public String ServerId = "";

    @JsonProperty("pwd")
    public String Password = "";

}
