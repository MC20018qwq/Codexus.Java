package codexus.cipher.entities.wpflauncher.minecraft;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/Minecraft/EntityCoreLibResponse.cs. */
@JsonPropertyOrder({"core_lib_md5","core_lib_name","core_lib_size","core_lib_url","mc_version","md5","name","refresh_time","size","url","version"})
public class EntityCoreLibResponse {
    @JsonProperty("core_lib_md5")
    @DotNetRequired
    public String CoreLibMd5;

    @JsonProperty("core_lib_name")
    @DotNetRequired
    public String CoreLibName;

    @JsonProperty("core_lib_size")
    @DotNetRequired
    public int CoreLibSize;

    @JsonProperty("core_lib_url")
    @DotNetRequired
    public String CoreLibUrl;

    @JsonProperty("mc_version")
    @DotNetRequired
    public int McVersion;

    @JsonProperty("md5")
    @DotNetRequired
    public String Md5;

    @JsonProperty("name")
    @DotNetRequired
    public String Name;

    @JsonProperty("refresh_time")
    @DotNetRequired
    public int RefreshTime;

    @JsonProperty("size")
    @DotNetRequired
    public int Size;

    @JsonProperty("url")
    @DotNetRequired
    public String Url;

    @JsonProperty("version")
    @DotNetRequired
    public String Version;

}
