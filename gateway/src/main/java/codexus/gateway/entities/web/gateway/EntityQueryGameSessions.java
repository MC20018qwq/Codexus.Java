package codexus.gateway.entities.web.gateway;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Gateway/Codexus/Gateway/Entities/Web/Gateway/EntityQueryGameSessions.cs. */
@JsonPropertyOrder({"id","name","game_type","is_rental","server_name","guid","character_name","server_version","status_text","type","progress_value","local_address"})
public class EntityQueryGameSessions {
    @JsonProperty("id")
    @DotNetRequired
    public String Id;

    @JsonProperty("name")
    @DotNetRequired
    public java.util.UUID Name = codexus.csharpsup.DotNetGuid.EMPTY;

    @JsonProperty("game_type")
    @DotNetRequired
    public String GameType;

    @JsonProperty("is_rental")
    @DotNetRequired
    public boolean IsRental;

    @JsonProperty("server_name")
    @DotNetRequired
    public String ServerName;

    @JsonProperty("guid")
    @DotNetRequired
    public String Guid;

    @JsonProperty("character_name")
    @DotNetRequired
    public String CharacterName;

    @JsonProperty("server_version")
    @DotNetRequired
    public String ServerVersion;

    @JsonProperty("status_text")
    @DotNetRequired
    public String StatusText;

    @JsonProperty("type")
    @DotNetRequired
    public String Type;

    @JsonProperty("progress_value")
    @DotNetRequired
    public int ProgressValue;

    @JsonProperty("local_address")
    public String LocalAddress = "";

}
