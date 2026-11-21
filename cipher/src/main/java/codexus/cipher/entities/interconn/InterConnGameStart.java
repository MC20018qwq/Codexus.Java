package codexus.cipher.entities.interconn;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/InterConn/InterConnGameStart.cs. */
@JsonPropertyOrder({"game_id","game_type","strict_mode","item_list"})
public class InterConnGameStart {
    @JsonProperty("game_id")
    @DotNetRequired
    public String GameId;

    @JsonProperty("game_type")
    public String GameType = "2";

    @JsonProperty("strict_mode")
    public boolean StrictMode = true;

    @JsonProperty("item_list")
    @DotNetRequired
    public String[] ItemList;

}
