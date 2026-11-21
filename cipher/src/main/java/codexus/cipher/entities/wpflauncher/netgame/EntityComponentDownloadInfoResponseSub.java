package codexus.cipher.entities.wpflauncher.netgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/NetGame/EntityComponentDownloadInfoResponseSub.cs. */
@JsonPropertyOrder({"java_version","mc_version_name","res_url","res_size","res_md5","jar_md5","res_name","res_version"})
public class EntityComponentDownloadInfoResponseSub {
    @JsonProperty("java_version")
    @DotNetRequired
    public int JavaVersion;

    @JsonProperty("mc_version_name")
    @DotNetRequired
    public String McVersionName;

    @JsonProperty("res_url")
    @DotNetRequired
    public String ResUrl;

    @JsonProperty("res_size")
    @DotNetRequired
    public long ResSize;

    @JsonProperty("res_md5")
    @DotNetRequired
    public String ResMd5;

    @JsonProperty("jar_md5")
    @DotNetRequired
    public String JarMd5;

    @JsonProperty("res_name")
    @DotNetRequired
    public String ResName;

    @JsonProperty("res_version")
    @DotNetRequired
    public int ResVersion;

}
