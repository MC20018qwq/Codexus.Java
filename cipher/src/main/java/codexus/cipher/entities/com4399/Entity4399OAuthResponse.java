package codexus.cipher.entities.com4399;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/Com4399/Entity4399OAuthResponse.cs. */
@JsonPropertyOrder({"code","message","result"})
public class Entity4399OAuthResponse {
    @JsonProperty("code")
    public int Code;

    @JsonProperty("message")
    public String Message = "";

    @JsonProperty("result")
    public codexus.cipher.entities.com4399.Entity4399OAuthResult Result = new codexus.cipher.entities.com4399.Entity4399OAuthResult();

}
