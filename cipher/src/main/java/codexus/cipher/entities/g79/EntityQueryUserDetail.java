package codexus.cipher.entities.g79;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/G79/EntityQueryUserDetail.cs. */
@JsonPropertyOrder({"version"})
public class EntityQueryUserDetail {
    @JsonProperty("version")
    @DotNetRequired
    public codexus.csharpsup.DotNetVersion Version;

}
