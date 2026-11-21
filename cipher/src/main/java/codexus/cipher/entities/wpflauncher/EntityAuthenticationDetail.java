package codexus.cipher.entities.wpflauncher;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/EntityAuthenticationDetail.cs. */
@JsonPropertyOrder({"os_name","os_ver","mac_addr","udid","app_ver","sdk_ver","network","disk","is64bit","video_card1","video_card2","video_card3","video_card4","launcher_type","pay_channel","dotnet_ver","cpu_type","ram_size","device_width","device_height","os_detail"})
public class EntityAuthenticationDetail {
    @JsonProperty("os_name")
    public String OsName = "windows";

    @JsonProperty("os_ver")
    public String OsVer = "Microsoft Windows 11 专业版";

    @JsonProperty("mac_addr")
    public String MacAddr = codexus.cipher.utils.StringGenerator.generateRandomMacAddress("", true);

    @JsonProperty("udid")
    @DotNetRequired
    public String Udid;

    @JsonProperty("app_ver")
    @DotNetRequired
    public String AppVersion;

    @JsonProperty("sdk_ver")
    public String SdkVersion = "";

    @JsonProperty("network")
    public String Network = "";

    @JsonProperty("disk")
    @DotNetRequired
    public String Disk;

    @JsonProperty("is64bit")
    public String Is64Bit = "1";

    @JsonProperty("video_card1")
    public String VideoCard1 = "Microsoft Hyper-V 视频";

    @JsonProperty("video_card2")
    public String VideoCard2 = "Microsoft Remote Display Adapter";

    @JsonProperty("video_card3")
    public String VideoCard3 = "";

    @JsonProperty("video_card4")
    public String VideoCard4 = "";

    @JsonProperty("launcher_type")
    public String LauncherType = "PC_java";

    @JsonProperty("pay_channel")
    @DotNetRequired
    public String PayChannel;

    @JsonProperty("dotnet_ver")
    public String DotnetVersion = "4.8.0";

    @JsonProperty("cpu_type")
    public String CpuType = "Intel(R) Core(TM) i9-14900KF";

    @JsonProperty("ram_size")
    public String RamSize = "8589934592";

    @JsonProperty("device_width")
    public String DeviceWidth = "1920";

    @JsonProperty("device_height")
    public String DeviceHeight = "1080";

    @JsonProperty("os_detail")
    public String OsDetail = "10.0.26100";

}
