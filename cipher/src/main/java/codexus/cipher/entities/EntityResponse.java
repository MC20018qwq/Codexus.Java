package codexus.cipher.entities;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/EntityResponse.cs. */
@JsonPropertyOrder({"code","message"})
public class EntityResponse {
    @JsonProperty("code")
    public int Code;

    @JsonProperty("message")
    public String Message = "";

}
