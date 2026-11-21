package codexus.gateway.entities.web.gateway;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Gateway/Codexus/Gateway/Entities/Web/Gateway/EntityQueryInterceptors.cs. */
@JsonPropertyOrder({"id","game_type","is_rental","name","address","role","server","version","local"})
public class EntityQueryInterceptors {
    @JsonProperty("id")
    @DotNetRequired
    public String Id;

    @JsonProperty("game_type")
    @DotNetRequired
    public String GameType;

    @JsonProperty("is_rental")
    @DotNetRequired
    public boolean IsRental;

    @JsonProperty("name")
    @DotNetRequired
    public java.util.UUID Name = codexus.csharpsup.DotNetGuid.EMPTY;

    @JsonProperty("address")
    @DotNetRequired
    public String Address;

    @JsonProperty("role")
    @DotNetRequired
    public String Role;

    @JsonProperty("server")
    @DotNetRequired
    public String Server;

    @JsonProperty("version")
    @DotNetRequired
    public String Version;

    @JsonProperty("local")
    @DotNetRequired
    public String LocalAddress;

}
