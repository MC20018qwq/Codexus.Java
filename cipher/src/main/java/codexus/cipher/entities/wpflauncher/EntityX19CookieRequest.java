package codexus.cipher.entities.wpflauncher;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/EntityX19CookieRequest.cs. */
@JsonPropertyOrder({"sauth_json"})
public class EntityX19CookieRequest {
    @JsonProperty("sauth_json")
    @DotNetRequired
    public String Json;

}
