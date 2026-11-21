package codexus.cipher.entities.wpflauncher.netgame.mods;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/NetGame/Mods/EntityModsInfo.cs. */
@JsonPropertyOrder({"modPath","name","id","iid","md5","version"})
public class EntityModsInfo {
    @JsonProperty("modPath")
    @DotNetRequired
    public String ModPath;

    @JsonProperty("name")
    @DotNetRequired
    public String Name = "";

    @JsonProperty("id")
    @DotNetRequired
    public String Id;

    @JsonProperty("iid")
    @DotNetRequired
    public String Iid;

    @JsonProperty("md5")
    @DotNetRequired
    public String Md5;

    @JsonProperty("version")
    @DotNetRequired
    public String Version = "";

}
