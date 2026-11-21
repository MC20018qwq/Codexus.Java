package codexus.cipher.entities.wpflauncher.rentalgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/RentalGame/EntityQueryRentalGamePlayerList.cs. */
@JsonPropertyOrder({"server_id","offset","length"})
public class EntityQueryRentalGamePlayerList {
    @JsonProperty("server_id")
    public String ServerId = "";

    @JsonProperty("offset")
    public int Offset;

    @JsonProperty("length")
    public int Length;

}
