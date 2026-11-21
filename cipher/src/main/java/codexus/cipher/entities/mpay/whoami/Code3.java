package codexus.cipher.entities.mpay.whoami;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/MPay/WhoAmi/Code3.cs. */
@JsonPropertyOrder({"iso_code","names"})
public class Code3 {
    @JsonProperty("iso_code")
    public String IsoCode;

    @JsonProperty("names")
    public codexus.cipher.entities.mpay.whoami.Names Names;

}
