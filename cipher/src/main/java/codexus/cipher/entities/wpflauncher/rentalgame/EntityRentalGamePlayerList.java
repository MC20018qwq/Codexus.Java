package codexus.cipher.entities.wpflauncher.rentalgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/RentalGame/EntityRentalGamePlayerList.cs. */
@JsonPropertyOrder({"entity_id","server_id","user_id","name","create_ts","delete_ts","is_online","status"})
public class EntityRentalGamePlayerList {
    @JsonProperty("entity_id")
    public String EntityId = "";

    @JsonProperty("server_id")
    public String ServerId = "";

    @JsonProperty("user_id")
    public String UserId = "";

    @JsonProperty("name")
    public String Name = "";

    @JsonProperty("create_ts")
    @Unsigned(64)
    public java.math.BigInteger CreateTs = java.math.BigInteger.ZERO;

    @JsonProperty("delete_ts")
    @Unsigned(64)
    public java.math.BigInteger DeleteTs = java.math.BigInteger.ZERO;

    @JsonProperty("is_online")
    public boolean IsOnline;

    @JsonProperty("status")
    public int Status = 0;

    /** Known enum value, or null when the raw code is undeclared. */
    public codexus.cipher.entities.wpflauncher.rentalgame.EnumPlayerStatus getStatus() {
        return codexus.cipher.entities.wpflauncher.rentalgame.EnumPlayerStatus.fromValue(Status);
    }

    public void setStatus(codexus.cipher.entities.wpflauncher.rentalgame.EnumPlayerStatus value) {
        Status = java.util.Objects.requireNonNull(value).value();
    }

}
