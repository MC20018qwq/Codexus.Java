package codexus.cipher.entities.mgbsdk;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/MgbSdk/EntityMgbSdkCookie.cs. */
@JsonPropertyOrder({"timestamp","userid","realname","gameid","login_channel","app_channel","platform","sdkuid","sessionid","sdk_version","udid","deviceid","aim_info","client_login_sn","gas_token","source_platform","ip"})
public class EntityMgbSdkCookie {
    @JsonProperty("timestamp")
    @DotNetRequired
    public String Timestamp;

    @JsonProperty("userid")
    @DotNetRequired
    public String UserId;

    @JsonProperty("realname")
    public String RealName = "{\"realname_type\":\"0\"}";

    @JsonProperty("gameid")
    @DotNetRequired
    public String GameId;

    @JsonProperty("login_channel")
    @DotNetRequired
    public String LoginChannel;

    @JsonProperty("app_channel")
    @DotNetRequired
    public String AppChannel;

    @JsonProperty("platform")
    public String Platform = "pc";

    @JsonProperty("sdkuid")
    @DotNetRequired
    public String SdkUid;

    @JsonProperty("sessionid")
    @DotNetRequired
    public String SessionId;

    @JsonProperty("sdk_version")
    public String SdkVersion = "1.0.0";

    @JsonProperty("udid")
    @DotNetRequired
    public String Udid;

    @JsonProperty("deviceid")
    @DotNetRequired
    public String DeviceId;

    @JsonProperty("aim_info")
    public String AimInfo = "{\"aim\":\"127.0.0.1\",\"tz\":\"+0800\",\"tzid\":\"\",\"country\":\"CN\"}";

    @JsonProperty("client_login_sn")
    @DotNetRequired
    public String ClientLoginSn;

    @JsonProperty("gas_token")
    public String GasToken = "";

    @JsonProperty("source_platform")
    public String SourcePlatform = "pc";

    @JsonProperty("ip")
    public String Ip = "127.0.0.1";

}
