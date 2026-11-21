package codexus.cipher.entities.mpay.whoami;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/MPay/WhoAmi/Names.cs. */
@JsonPropertyOrder({"en"})
public class Names {
    @JsonProperty("en")
    public String En;

}
