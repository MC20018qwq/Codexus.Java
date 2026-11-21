package codexus.cipher.entities.wpflauncher.rentalgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/RentalGame/EntityAddRentalGameRole.cs. */
@JsonPropertyOrder({"server_id","user_id","name","create_ts","is_online","status"})
public class EntityAddRentalGameRole {
    @JsonProperty("server_id")
    public String ServerId = "";

    @JsonProperty("user_id")
    public String UserId = "";

    @JsonProperty("name")
    public String Name = "";

    @JsonProperty("create_ts")
    public int CreateTs;

    @JsonProperty("is_online")
    public boolean IsOnline;

    @JsonProperty("status")
    public int Status;

}
