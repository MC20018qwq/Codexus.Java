package codexus.cipher.entities.wpflauncher.netgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/NetGame/EntityQueryNetGameItem.cs. */
@JsonPropertyOrder({"title_image_url"})
public class EntityQueryNetGameItem {
    @JsonProperty("title_image_url")
    public String TitleImageUrl = "";

}
