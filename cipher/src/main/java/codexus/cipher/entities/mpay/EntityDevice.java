package codexus.cipher.entities.mpay;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/MPay/EntityDevice.cs. */
@JsonPropertyOrder({"id","key"})
public class EntityDevice {
    @JsonProperty("id")
    public String Id = "";

    @JsonProperty("key")
    public String Key = "";

}
