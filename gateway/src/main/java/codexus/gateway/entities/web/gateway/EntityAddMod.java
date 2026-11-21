package codexus.gateway.entities.web.gateway;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Gateway/Codexus/Gateway/Entities/Web/Gateway/EntityAddMod.cs. */
@JsonPropertyOrder({"file_name","base64"})
public class EntityAddMod {
    @JsonProperty("file_name")
    public String FileName = "";

    @JsonProperty("base64")
    public String Base64 = "";

}
