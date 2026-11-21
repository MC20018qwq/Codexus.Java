package codexus.gateway.entities.web.gateway;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Gateway/Codexus/Gateway/Entities/Web/Gateway/EntityUpdateRequest.cs. */
@JsonPropertyOrder({"id","token"})
public class EntityUpdateRequest {
    @JsonProperty("id")
    public String UserId = "";

    @JsonProperty("token")
    public String AccessToken = "";

}
