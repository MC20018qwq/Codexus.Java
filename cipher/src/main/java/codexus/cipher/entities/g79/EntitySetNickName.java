package codexus.cipher.entities.g79;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/G79/EntitySetNickName.cs. */
@JsonPropertyOrder({"name"})
public class EntitySetNickName {
    @JsonProperty("name")
    @DotNetRequired
    public String Name;

}
