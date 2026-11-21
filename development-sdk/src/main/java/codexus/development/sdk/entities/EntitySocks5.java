package codexus.development.sdk.entities;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Development.SDK/Codexus/Development/SDK/Entities/EntitySocks5.cs. */
@JsonPropertyOrder({"enabled","address","port","username","password"})
public class EntitySocks5 {
    @JsonProperty("enabled")
    public boolean Enabled;

    @JsonProperty("address")
    public String Address = "127.0.0.1";

    @JsonProperty("port")
    public int Port = 1080;

    @JsonProperty("username")
    public String Username;

    @JsonProperty("password")
    public String Password;

}
