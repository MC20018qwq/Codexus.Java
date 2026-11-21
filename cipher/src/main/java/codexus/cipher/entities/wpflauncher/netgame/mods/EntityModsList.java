package codexus.cipher.entities.wpflauncher.netgame.mods;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/NetGame/Mods/EntityModsList.cs. */
@JsonPropertyOrder({"mods"})
public class EntityModsList {
    @JsonProperty("mods")
    public java.util.List<codexus.cipher.entities.wpflauncher.netgame.mods.EntityModsInfo> Mods = new java.util.ArrayList<>();

}
