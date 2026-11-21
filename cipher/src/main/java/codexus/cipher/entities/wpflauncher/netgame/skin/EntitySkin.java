package codexus.cipher.entities.wpflauncher.netgame.skin;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/NetGame/Skin/EntitySkin.cs. */
@JsonPropertyOrder({"entity_id","brief_summary","name","like_num","is_has","title_image_url","points","diamonds"})
public class EntitySkin {
    @JsonProperty("entity_id")
    public String EntityId = "";

    @JsonProperty("brief_summary")
    public String BriefSummary = "";

    @JsonProperty("name")
    public String Name = "";

    @JsonProperty("like_num")
    public int LikeNum;

    @JsonProperty("is_has")
    public boolean IsHas;

    @JsonProperty("title_image_url")
    public String TitleImageUrl = "";

    @JsonProperty("points")
    @Unsigned(64)
    public java.math.BigInteger Points = java.math.BigInteger.ZERO;

    @JsonProperty("diamonds")
    @Unsigned(64)
    public java.math.BigInteger Diamonds = java.math.BigInteger.ZERO;

}
