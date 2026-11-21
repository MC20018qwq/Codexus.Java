package codexus.cipher.entities;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/Entities.cs. */
@SuppressWarnings("unchecked")
@JsonPropertyOrder({"details","entities","total","code","message"})
public class Entities<T> extends codexus.cipher.entities.EntityResponse {
    @JsonProperty("details")
    public String Details = "";

    @JsonProperty("entities")
    public T[] Data = (T[]) new Object[0];

    @JsonProperty("total")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = codexus.cipher.entities.converter.NetEaseStringConverter.class)
    public int Total;

}
