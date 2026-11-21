package codexus.cipher.entities.wpflauncher.rentalgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/RentalGame/EntityDeleteRentalGameRole.cs. */
@JsonPropertyOrder({"entity_id"})
public class EntityDeleteRentalGameRole {
    @JsonProperty("entity_id")
    public String EntityId = "";

}
