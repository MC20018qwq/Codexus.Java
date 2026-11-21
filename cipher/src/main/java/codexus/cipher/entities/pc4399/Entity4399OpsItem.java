package codexus.cipher.entities.pc4399;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/Pc4399/Entity4399OpsItem.cs. */
@JsonPropertyOrder({"name","link","banner","type"})
public class Entity4399OpsItem {
    @JsonProperty("name")
    public String Name = "";

    @JsonProperty("link")
    public String Link = "";

    @JsonProperty("banner")
    public String Banner = "";

    @JsonProperty("type")
    public int Type;

}
