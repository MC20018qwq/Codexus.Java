package codexus.development.sdk.entities;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Development.SDK/Codexus/Development/SDK/Entities/EntityAvailableUser.cs. */
@JsonPropertyOrder({"id","token","last_login_time"})
public class EntityAvailableUser {
    @JsonProperty("id")
    @DotNetRequired
    public String UserId;

    @JsonProperty("token")
    @DotNetRequired
    public String AccessToken;

    @JsonProperty("last_login_time")
    @DotNetRequired
    public long LastLoginTime;

}
