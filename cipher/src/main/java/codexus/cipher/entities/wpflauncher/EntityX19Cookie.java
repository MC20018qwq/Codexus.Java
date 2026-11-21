package codexus.cipher.entities.wpflauncher;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/EntityX19Cookie.cs. */
@JsonPropertyOrder({"gameid","login_channel","app_channel","platform","sdkuid","sessionid","sdk_version","udid","deviceid","aim_info"})
public class EntityX19Cookie {
    @JsonProperty("gameid")
    public String GameId = "x19";

    @JsonProperty("login_channel")
    public String LoginChannel = "netease";

    @JsonProperty("app_channel")
    public String AppChannel = "netease";

    @JsonProperty("platform")
    public String Platform = "pc";

    @JsonProperty("sdkuid")
    @DotNetRequired
    public String SdkUid;

    @JsonProperty("sessionid")
    @DotNetRequired
    public String SessionId;

    @JsonProperty("sdk_version")
    public String SdkVersion = "4.2.0";

    @JsonProperty("udid")
    @DotNetRequired
    public String Udid;

    @JsonProperty("deviceid")
    @DotNetRequired
    public String DeviceId;

    @JsonProperty("aim_info")
    public String AimInfo = "{\"aim\":\"127.0.0.1\",\"country\":\"CN\",\"tz\":\"+0800\",\"tzid\":\"\"}";

}
