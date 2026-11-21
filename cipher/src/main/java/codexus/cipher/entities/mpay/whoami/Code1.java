package codexus.cipher.entities.mpay.whoami;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/MPay/WhoAmi/Code1.cs. */
@JsonPropertyOrder({"code","names"})
public class Code1 {
    @JsonProperty("code")
    public String Code;

    @JsonProperty("names")
    public codexus.cipher.entities.mpay.whoami.Names Names;

}
