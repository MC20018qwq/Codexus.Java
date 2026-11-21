package codexus.cipher.entities.wpflauncher.netgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/NetGame/EntityMcVersion.cs. */
@JsonPropertyOrder({"mcversionid","name"})
public class EntityMcVersion {
    @JsonProperty("mcversionid")
    public int McVersionId;

    @JsonProperty("name")
    public String Name = "";

}
