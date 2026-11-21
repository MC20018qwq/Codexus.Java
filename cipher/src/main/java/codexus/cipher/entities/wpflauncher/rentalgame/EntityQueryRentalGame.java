package codexus.cipher.entities.wpflauncher.rentalgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/RentalGame/EntityQueryRentalGame.cs. */
@JsonPropertyOrder({"offset","sort_type"})
public class EntityQueryRentalGame {
    @JsonProperty("offset")
    public int Offset;

    @JsonProperty("sort_type")
    public int SortType;

}
