package codexus.cipher.entities.wpflauncher;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/EntityAuthenticationVersion.cs. */
@JsonPropertyOrder({"version","launcher_md5","updater_md5"})
public class EntityAuthenticationVersion {
    @JsonProperty("version")
    @DotNetRequired
    public String Version;

    @JsonProperty("launcher_md5")
    public String LauncherMd5 = "";

    @JsonProperty("updater_md5")
    public String UpdaterMd5 = "";

}
