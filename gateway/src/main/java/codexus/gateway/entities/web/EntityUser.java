package codexus.gateway.entities.web;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Gateway/Codexus/Gateway/Entities/Web/EntityUser.cs. */
@JsonPropertyOrder({"id","authorized","auto_login","channel","type","details","platform","alias"})
public class EntityUser {
    @JsonProperty("id")
    @DotNetRequired
    public String UserId;

    @JsonProperty("authorized")
    @DotNetRequired
    public boolean Authorized;

    @JsonProperty("auto_login")
    @DotNetRequired
    public boolean AutoLogin;

    @JsonProperty("channel")
    @DotNetRequired
    public String Channel;

    @JsonProperty("type")
    @DotNetRequired
    public String Type;

    @JsonProperty("details")
    @DotNetRequired
    public String Details;

    @JsonProperty("platform")
    public int Platform = 0;

    @JsonProperty("alias")
    public String Alias = "";

    /** Known enum value, or null when the raw code is undeclared. */
    public codexus.gateway.enums.Platform getPlatform() {
        return codexus.gateway.enums.Platform.fromValue(Platform);
    }

    public void setPlatform(codexus.gateway.enums.Platform value) {
        Platform = java.util.Objects.requireNonNull(value).value();
    }

}
