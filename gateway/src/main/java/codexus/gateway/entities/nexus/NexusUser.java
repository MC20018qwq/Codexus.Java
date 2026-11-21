package codexus.gateway.entities.nexus;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Gateway/Codexus/Gateway/Entities/Nexus/NexusUser.cs. */
@JsonPropertyOrder({"Id","RefreshToken","AccessToken","RefreshExpireTime","AccessExpireTime"})
public class NexusUser {
    @JsonProperty("Id")
    public String Id = "";

    @JsonProperty("RefreshToken")
    public String RefreshToken = "";

    @JsonProperty("AccessToken")
    public String AccessToken = "";

    @JsonProperty("RefreshExpireTime")
    public String RefreshExpireTime = "";

    @JsonProperty("AccessExpireTime")
    public String AccessExpireTime = "";

}
