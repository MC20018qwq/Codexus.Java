package codexus.gateway.entities.web.gateway;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Gateway/Codexus/Gateway/Entities/Web/Gateway/EntityUpdateUserAlias.cs. */
@JsonPropertyOrder({"id","platform","alias"})
public class EntityUpdateUserAlias {
    @JsonProperty("id")
    @DotNetRequired
    public String EntityId;

    @JsonProperty("platform")
    @DotNetRequired
    public int Platform = 0;

    @JsonProperty("alias")
    @DotNetRequired
    public String Alias;

    /** Known enum value, or null when the raw code is undeclared. */
    public codexus.gateway.enums.Platform getPlatform() {
        return codexus.gateway.enums.Platform.fromValue(Platform);
    }

    public void setPlatform(codexus.gateway.enums.Platform value) {
        Platform = java.util.Objects.requireNonNull(value).value();
    }

}
