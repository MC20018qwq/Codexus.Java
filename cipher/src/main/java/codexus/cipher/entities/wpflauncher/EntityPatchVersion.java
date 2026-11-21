package codexus.cipher.entities.wpflauncher;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/EntityPatchVersion.cs. */
@JsonPropertyOrder({"size","url","md5"})
public class EntityPatchVersion {
    @JsonProperty("size")
    public long Size;

    @JsonProperty("url")
    public String Url = "";

    @JsonProperty("md5")
    public String Md5 = "";

}
