package codexus.cipher.entities;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/Entity.cs. */
@SuppressWarnings("unchecked")
@JsonPropertyOrder({"details","entity","code","message"})
public class Entity<T> extends codexus.cipher.entities.EntityResponse {
    @JsonProperty("details")
    public String Details = "";

    @JsonProperty("entity")
    public T Data;

}
