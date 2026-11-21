package codexus.cipher.entities.wpflauncher.rentalgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/RentalGame/EntityQueryRentalGameDetail.cs. */
@JsonPropertyOrder({"server_id"})
public class EntityQueryRentalGameDetail {
    @JsonProperty("server_id")
    public String ServerId = "";

}
