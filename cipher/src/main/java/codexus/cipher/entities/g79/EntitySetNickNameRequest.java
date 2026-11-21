package codexus.cipher.entities.g79;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/G79/EntitySetNickNameRequest.cs. */
@JsonPropertyOrder({"name","entity_id"})
public class EntitySetNickNameRequest {
    @JsonProperty("name")
    @DotNetRequired
    public String Name;

    @JsonProperty("entity_id")
    @DotNetRequired
    public String EntityId;

}
