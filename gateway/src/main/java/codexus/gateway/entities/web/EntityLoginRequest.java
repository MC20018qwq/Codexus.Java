package codexus.gateway.entities.web;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Gateway/Codexus/Gateway/Entities/Web/EntityLoginRequest.cs. */
@JsonPropertyOrder({"channel","type","details","platform","token"})
public class EntityLoginRequest {
    @JsonProperty("channel")
    public String Channel = "";

    @JsonProperty("type")
    public String Type = "";

    @JsonProperty("details")
    public String Details = "";

    @JsonProperty("platform")
    public int Platform = 0;

    @JsonProperty("token")
    public String Token = "";

    /** Known enum value, or null when the raw code is undeclared. */
    public codexus.gateway.enums.Platform getPlatform() {
        return codexus.gateway.enums.Platform.fromValue(Platform);
    }

    public void setPlatform(codexus.gateway.enums.Platform value) {
        Platform = java.util.Objects.requireNonNull(value).value();
    }

}
