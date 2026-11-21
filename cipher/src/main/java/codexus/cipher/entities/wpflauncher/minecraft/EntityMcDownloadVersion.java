package codexus.cipher.entities.wpflauncher.minecraft;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/Minecraft/EntityMcDownloadVersion.cs. */
@JsonPropertyOrder({"mc_version"})
public class EntityMcDownloadVersion {
    @JsonProperty("mc_version")
    @DotNetRequired
    public int McVersion;

}
