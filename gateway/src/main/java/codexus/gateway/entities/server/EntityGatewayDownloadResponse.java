package codexus.gateway.entities.server;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Gateway/Codexus/Gateway/Entities/Server/EntityGatewayDownloadResponse.cs. */
@JsonPropertyOrder({"url"})
public class EntityGatewayDownloadResponse {
    @JsonProperty("url")
    @DotNetRequired
    public String Url;

}
