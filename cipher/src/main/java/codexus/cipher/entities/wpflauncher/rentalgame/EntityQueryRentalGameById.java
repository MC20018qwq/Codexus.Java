package codexus.cipher.entities.wpflauncher.rentalgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/RentalGame/EntityQueryRentalGameById.cs. */
@JsonPropertyOrder({"offset","sort_type","world_name_key"})
public class EntityQueryRentalGameById {
    @JsonProperty("offset")
    @Unsigned(64)
    public java.math.BigInteger Offset = java.math.BigInteger.ZERO;

    @JsonProperty("sort_type")
    public int SortType = 0;

    @JsonProperty("world_name_key")
    public java.util.List<String> WorldNameKey = new java.util.ArrayList<>();

    /** Known enum value, or null when the raw code is undeclared. */
    public codexus.cipher.entities.wpflauncher.rentalgame.EnumSortType getSortType() {
        return codexus.cipher.entities.wpflauncher.rentalgame.EnumSortType.fromValue(SortType);
    }

    public void setSortType(codexus.cipher.entities.wpflauncher.rentalgame.EnumSortType value) {
        SortType = java.util.Objects.requireNonNull(value).value();
    }

}
