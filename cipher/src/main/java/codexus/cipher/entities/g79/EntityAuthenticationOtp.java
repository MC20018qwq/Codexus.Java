package codexus.cipher.entities.g79;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/G79/EntityAuthenticationOtp.cs. */
@JsonPropertyOrder({"entity_id","token"})
public class EntityAuthenticationOtp {
    @JsonProperty("entity_id")
    @DotNetRequired
    public String EntityId;

    @JsonProperty("token")
    @DotNetRequired
    public String Token;

}
