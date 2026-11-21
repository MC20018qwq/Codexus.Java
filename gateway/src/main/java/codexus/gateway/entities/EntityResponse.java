package codexus.gateway.entities;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Gateway/Codexus/Gateway/Entities/EntityResponse.cs. */
@JsonPropertyOrder({"code","message","payload"})
public class EntityResponse {
    @JsonProperty("code")
    public int Code;

    @JsonProperty("message")
    public String Message = "";

    @JsonProperty("payload")
    public String Payload = "";

    public static String success(int code, String payload) {EntityResponse r=new EntityResponse();r.Code=code;r.Message="Success";r.Payload=payload;return codexus.csharpsup.DotNetJson.serialize(r);}
    public static String success(String payload) {return success(0,payload);}
    public static String error(int code, Exception exception) {return error(code, exception.getMessage());}
    public static String error(int code, String message) {EntityResponse r=new EntityResponse();r.Code=code;r.Message=message;r.Payload="";return codexus.csharpsup.DotNetJson.serialize(r);}
}
