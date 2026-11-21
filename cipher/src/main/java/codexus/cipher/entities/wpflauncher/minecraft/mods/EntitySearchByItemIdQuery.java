package codexus.cipher.entities.wpflauncher.minecraft.mods;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/Minecraft/Mods/EntitySearchByItemIdQuery.cs. */
@JsonPropertyOrder({"item_id","length","offset"})
public class EntitySearchByItemIdQuery {
    @JsonProperty("item_id")
    @DotNetRequired
    public String ItemId;

    @JsonProperty("length")
    @DotNetRequired
    public int Length;

    @JsonProperty("offset")
    @DotNetRequired
    public int Offset;

}
