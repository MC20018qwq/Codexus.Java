package codexus.cipher.entities.wpflauncher.netgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/NetGame/EntitySearchByIdsQuery.cs. */
@JsonPropertyOrder({"item_id_list"})
public class EntitySearchByIdsQuery {
    @JsonProperty("item_id_list")
    @DotNetRequired
    public java.util.List<java.math.BigInteger> ItemIdList;

}
