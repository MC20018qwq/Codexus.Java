package codexus.cipher.entities.pc4399;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/Pc4399/Entity4399ResponseData.cs. */
@JsonPropertyOrder({"ops","username","login_tip","sdk_login_data"})
public class Entity4399ResponseData {
    @JsonProperty("ops")
    public java.util.List<codexus.cipher.entities.pc4399.Entity4399OpsItem> Ops = new java.util.ArrayList<>();

    @JsonProperty("username")
    public String Username = "";

    @JsonProperty("login_tip")
    public String LoginTip = "";

    @JsonProperty("sdk_login_data")
    public String SdkLoginData = "";

}
