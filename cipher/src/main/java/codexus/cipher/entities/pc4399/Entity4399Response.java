package codexus.cipher.entities.pc4399;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/Pc4399/Entity4399Response.cs. */
@JsonPropertyOrder({"code","msg","data"})
public class Entity4399Response {
    @JsonProperty("code")
    public int Code;

    @JsonProperty("msg")
    public String Msg = "";

    @JsonProperty("data")
    public codexus.cipher.entities.pc4399.Entity4399ResponseData Data = new codexus.cipher.entities.pc4399.Entity4399ResponseData();

}
