package codexus.gateway.entities.web.gateway;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Gateway/Codexus/Gateway/Entities/Web/Gateway/EntityCodeRequest.cs. */
@JsonPropertyOrder({"phone","code"})
public class EntityCodeRequest {
    @JsonProperty("phone")
    @DotNetRequired
    public String Phone;

    @JsonProperty("code")
    @DotNetRequired
    public String Code;

}
