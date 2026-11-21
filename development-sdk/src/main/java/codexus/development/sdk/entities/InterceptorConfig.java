package codexus.development.sdk.entities;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Development.SDK/Codexus/Development/SDK/Entities/InterceptorConfig.cs. */
@JsonPropertyOrder({"is_rental","local_address","local_port","nickname","forward_address","forward_port","server_name","server_version","mod_info","game_id","user_id","user_token"})
public class InterceptorConfig {
    @JsonProperty("is_rental")
    @DotNetRequired
    public boolean IsRental;

    @JsonProperty("local_address")
    @DotNetRequired
    public String LocalAddress;

    @JsonProperty("local_port")
    @DotNetRequired
    public int LocalPort;

    @JsonProperty("nickname")
    @DotNetRequired
    public String NickName;

    @JsonProperty("forward_address")
    @DotNetRequired
    public String ForwardAddress;

    @JsonProperty("forward_port")
    @DotNetRequired
    public int ForwardPort;

    @JsonProperty("server_name")
    @DotNetRequired
    public String ServerName;

    @JsonProperty("server_version")
    @DotNetRequired
    public String ServerVersion;

    @JsonProperty("mod_info")
    @DotNetRequired
    public String ModInfo;

    @JsonProperty("game_id")
    @DotNetRequired
    public String GameId;

    @JsonProperty("user_id")
    @DotNetRequired
    public String UserId;

    @JsonProperty("user_token")
    @DotNetRequired
    public String UserToken;

    @JsonIgnore
    public java.util.function.BiConsumer<codexus.development.sdk.entities.InterceptorConfig, String> OnJoinServer;

    @JsonIgnore
    public codexus.development.sdk.entities.EntitySocks5 Socks5 = new codexus.development.sdk.entities.EntitySocks5();

    public InterceptorConfig clone() {
        InterceptorConfig c=new InterceptorConfig();
        c.IsRental=IsRental;c.LocalAddress=LocalAddress;c.LocalPort=LocalPort;c.NickName=NickName;
        c.ForwardAddress=ForwardAddress;c.ForwardPort=ForwardPort;c.ServerName=ServerName;c.ServerVersion=ServerVersion;
        c.ModInfo=ModInfo;c.GameId=GameId;c.UserId=UserId;c.UserToken=UserToken;c.OnJoinServer=OnJoinServer;c.Socks5=Socks5;
        return c;
    }
}
