package codexus.cipher.entities.wpflauncher.netgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/NetGame/EntityCreateCharacter.cs. */
@JsonPropertyOrder({"game_id","game_type","user_id","name","create_time","expire_time"})
public class EntityCreateCharacter {
    @JsonProperty("game_id")
    @DotNetRequired
    public String GameId;

    @JsonProperty("game_type")
    public int GameType = 2;

    @JsonProperty("user_id")
    @DotNetRequired
    public String UserId;

    @JsonProperty("name")
    @DotNetRequired
    public String Name;

    @JsonProperty("create_time")
    public int CreateTime = 555555;

    @JsonProperty("expire_time")
    public int ExpireTime;

}
