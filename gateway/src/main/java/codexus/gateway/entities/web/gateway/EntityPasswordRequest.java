package codexus.gateway.entities.web.gateway;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Gateway/Codexus/Gateway/Entities/Web/Gateway/EntityPasswordRequest.cs. */
@JsonPropertyOrder({"account","password","captcha_identifier","captcha"})
public class EntityPasswordRequest {
    @JsonProperty("account")
    @DotNetRequired
    public String Account;

    @JsonProperty("password")
    @DotNetRequired
    public String Password;

    @JsonProperty("captcha_identifier")
    public String CaptchaIdentifier;

    @JsonProperty("captcha")
    public String Captcha;

}
