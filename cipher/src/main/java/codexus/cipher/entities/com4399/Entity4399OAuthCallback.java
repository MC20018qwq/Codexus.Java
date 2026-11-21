package codexus.cipher.entities.com4399;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/Com4399/Entity4399OAuthCallback.cs. */
@JsonPropertyOrder({"code","message","result"})
public class Entity4399OAuthCallback {
    @JsonProperty("code")
    public String Code = "";

    @JsonProperty("message")
    public String Message = "";

    @JsonProperty("result")
    public String Result = "";

}
