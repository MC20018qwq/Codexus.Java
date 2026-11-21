package codexus.cipher.entities.com4399;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/Com4399/Entity4399OAuth.cs. */
@JsonPropertyOrder({"DEVICE_IDENTIFIER","SCREEN_RESOLUTION","DEVICE_MODEL","DEVICE_MODEL_VERSION","SYSTEM_VERSION","PLATFORM_TYPE","SDK_VERSION","GAME_KEY","GAME_VERSION","BID","RUNTIME","CANAL_IDENTIFIER","UDID","DEBUG","NETWORK_TYPE","GAME_BOX_VERSION","VIP_INFO","TEAM","DEVICE_IDENTIFIER_SM","UID"})
public class Entity4399OAuth {
    @JsonProperty("DEVICE_IDENTIFIER")
    @DotNetRequired
    public String DeviceIdentifier;

    @JsonProperty("SCREEN_RESOLUTION")
    public String ScreenResolution = "3200*1384";

    @JsonProperty("DEVICE_MODEL")
    public String DeviceModel = "Oppo A5 2022";

    @JsonProperty("DEVICE_MODEL_VERSION")
    public String DeviceModelVersion = "14";

    @JsonProperty("SYSTEM_VERSION")
    public String SystemVersion = "14";

    @JsonProperty("PLATFORM_TYPE")
    public String PlatformType = "Android";

    @JsonProperty("SDK_VERSION")
    public String SdkVersion = "3.12.2.503";

    @JsonProperty("GAME_KEY")
    public String GameKey = "115716";

    @JsonProperty("GAME_VERSION")
    public String GameVersion = "3.1.5.260925";

    @JsonProperty("BID")
    public String Bid = "com.netease.mc.m4399";

    @JsonProperty("RUNTIME")
    public String Runtime = "Origin";

    @JsonProperty("CANAL_IDENTIFIER")
    public String CanalIdentifier = "";

    @JsonProperty("UDID")
    @DotNetRequired
    public String Udid;

    @JsonProperty("DEBUG")
    public String Debug = "false";

    @JsonProperty("NETWORK_TYPE")
    public String NetworkType = "WIFI";

    @JsonProperty("GAME_BOX_VERSION")
    public String GameBoxVersion = "";

    @JsonProperty("VIP_INFO")
    public String VipInfo = "";

    @JsonProperty("TEAM")
    public int Team = 2;

    @JsonProperty("DEVICE_IDENTIFIER_SM")
    @DotNetRequired
    public String DeviceIdentifierSm;

    @JsonProperty("UID")
    public String Uid = "";

}
