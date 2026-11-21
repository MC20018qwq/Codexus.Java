package codexus.cipher.entities.mpay.whoami;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/MPay/WhoAmi/EntityWhoAmi.cs. */
@JsonPropertyOrder({"payload","sig","status","why"})
public class EntityWhoAmi {
    @JsonProperty("payload")
    public String Payload;

    @JsonProperty("sig")
    public String Signature;

    @JsonProperty("status")
    public int Status;

    @JsonProperty("why")
    public String Why;

}
