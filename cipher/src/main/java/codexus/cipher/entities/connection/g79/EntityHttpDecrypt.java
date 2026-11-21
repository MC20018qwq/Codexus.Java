package codexus.cipher.entities.connection.g79;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/Connection/G79/EntityHttpDecrypt.cs. */
@JsonPropertyOrder({"body"})
public class EntityHttpDecrypt {
    @JsonProperty("body")
    @DotNetRequired
    public String Body;

}
