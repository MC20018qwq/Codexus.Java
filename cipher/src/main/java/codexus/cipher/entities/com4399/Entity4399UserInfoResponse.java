package codexus.cipher.entities.com4399;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/Com4399/Entity4399UserInfoResponse.cs. */
@JsonPropertyOrder({"code","message","result"})
public class Entity4399UserInfoResponse {
    @JsonProperty("code")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = codexus.cipher.entities.converter.NetEaseIntConverter.class)
    public String Code = "";

    @JsonProperty("message")
    public String Message = "";

    @JsonProperty("result")
    public codexus.cipher.entities.com4399.Entity4399UserInfoResult Result = new codexus.cipher.entities.com4399.Entity4399UserInfoResult();

}
