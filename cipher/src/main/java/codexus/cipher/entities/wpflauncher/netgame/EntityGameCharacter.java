package codexus.cipher.entities.wpflauncher.netgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/NetGame/EntityGameCharacter.cs. */
@JsonPropertyOrder({"game_id","game_type","user_id","name","create_time","expire_time"})
public class EntityGameCharacter {
    @JsonProperty("game_id")
    public String GameId = "";

    @JsonProperty("game_type")
    public int GameType = 2;

    @JsonProperty("user_id")
    public String UserId = "";

    @JsonProperty("name")
    public String Name = "";

    @JsonProperty("create_time")
    public int CreateTime = 555555;

    @JsonProperty("expire_time")
    public int ExpireTime;

}
