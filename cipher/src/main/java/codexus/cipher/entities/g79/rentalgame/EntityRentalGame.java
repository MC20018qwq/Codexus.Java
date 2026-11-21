package codexus.cipher.entities.g79.rentalgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/G79/RentalGame/EntityRentalGame.cs. */
@JsonPropertyOrder({"entity_id","name","player_count","server_name"})
public class EntityRentalGame {
    @JsonProperty("entity_id")
    @DotNetRequired
    public String EntityId;

    @JsonProperty("name")
    @DotNetRequired
    public String Name;

    @JsonProperty("player_count")
    @DotNetRequired
    public int PlayerCount;

    @JsonProperty("server_name")
    @DotNetRequired
    public String ServerName;

}
