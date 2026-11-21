package codexus.cipher.entities.mpay.whoami;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/MPay/WhoAmi/Code4.cs. */
@JsonPropertyOrder({"id","names"})
public class Code4 {
    @JsonProperty("id")
    public int Id;

    @JsonProperty("names")
    public codexus.cipher.entities.mpay.whoami.Names Names;

}
