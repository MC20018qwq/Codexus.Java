package codexus.gateway.entities.web.gateway;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Gateway/Codexus/Gateway/Entities/Web/Gateway/EntityActiveWithCaptcha.cs. */
@JsonPropertyOrder({"id","identifier","captcha"})
public class EntityActiveWithCaptcha {
    @JsonProperty("id")
    @DotNetRequired
    public String UserId;

    @JsonProperty("identifier")
    @DotNetRequired
    public String Identifier;

    @JsonProperty("captcha")
    @DotNetRequired
    public String Captcha;

}
