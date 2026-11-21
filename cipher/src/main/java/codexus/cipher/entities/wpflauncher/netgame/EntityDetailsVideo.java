package codexus.cipher.entities.wpflauncher.netgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/NetGame/EntityDetailsVideo.cs. */
@JsonPropertyOrder({"cover","size","url"})
public class EntityDetailsVideo {
    @JsonProperty("cover")
    public String Cover = "";

    @JsonProperty("size")
    public int Size;

    @JsonProperty("url")
    public String Url = "";

}
