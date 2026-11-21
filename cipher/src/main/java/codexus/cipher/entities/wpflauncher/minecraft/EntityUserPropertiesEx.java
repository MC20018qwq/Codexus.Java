package codexus.cipher.entities.wpflauncher.minecraft;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/Minecraft/EntityUserPropertiesEx.cs. */
@JsonPropertyOrder({"GameType","isFilter","channel","timedelta","launcherVersion"})
public class EntityUserPropertiesEx {
    @JsonProperty("GameType")
    @DotNetRequired
    public int GameType;

    @JsonProperty("isFilter")
    @DotNetRequired
    public boolean IsFilter;

    @JsonProperty("channel")
    @DotNetRequired
    public String Channel;

    @JsonProperty("timedelta")
    @DotNetRequired
    public int TimeDelta;

    @JsonProperty("launcherVersion")
    @DotNetRequired
    public String LauncherVersion;

}
