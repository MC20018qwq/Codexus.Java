package codexus.cipher.entities.mpay;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/MPay/EntityVerifyResponse.cs. */
@JsonPropertyOrder({"reason","code","verify_url"})
public class EntityVerifyResponse {
    @JsonProperty("reason")
    public String Reason = "";

    @JsonProperty("code")
    public int Code;

    @JsonProperty("verify_url")
    public String VerifyUrl = "";

}
