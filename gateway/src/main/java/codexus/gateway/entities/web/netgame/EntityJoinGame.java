package codexus.gateway.entities.web.netgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Gateway/Codexus/Gateway/Entities/Web/NetGame/EntityJoinGame.cs. */
@JsonPropertyOrder({"id","name","game","role","vid","version","ip","port","nid","token","socks5"})
public class EntityJoinGame {
    @JsonProperty("id")
    public String UserId = "";

    @JsonProperty("name")
    public String GameName = "";

    @JsonProperty("game")
    public String GameId = "";

    @JsonProperty("role")
    public String Role = "";

    @JsonProperty("vid")
    public int VersionId;

    @JsonProperty("version")
    public String Version = "";

    @JsonProperty("ip")
    public String ServerIp = "";

    @JsonProperty("port")
    public int ServerPort;

    @JsonProperty("nid")
    public String NexusId = "";

    @JsonProperty("token")
    public String NexusToken = "";

    @JsonProperty("socks5")
    public codexus.development.sdk.entities.EntitySocks5 Socks5 = new codexus.development.sdk.entities.EntitySocks5();

}
