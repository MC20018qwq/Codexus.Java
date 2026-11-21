package codexus.cipher.entities.com4399;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/Com4399/Entity4399VipInfo.cs. */
@JsonPropertyOrder({"level","score"})
public class Entity4399VipInfo {
    @JsonProperty("level")
    public int Level;

    @JsonProperty("score")
    public int Score;

}
