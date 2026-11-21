package codexus.cipher.entities.g79;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/G79/Entities.cs. */
@SuppressWarnings("unchecked")
@JsonPropertyOrder({"details","entity","total","code","message"})
public class Entities<T> extends codexus.cipher.entities.EntityResponse {
    @JsonProperty("details")
    public String Details = "";

    @JsonProperty("entity")
    public T Data;

    @JsonProperty("total")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = codexus.cipher.entities.converter.NetEaseStringConverter.class)
    public int Total;

}
