package codexus.cipher.entities.wpflauncher.rentalgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/RentalGame/EntityRentalGame.cs. */
@JsonPropertyOrder({"entity_id","name","server_name","visibility","has_pwd","server_type","status","capacity","mc_version","owner_id","player_count","image_url","world_id","min_level","pvp","like_num","icon_index","offset"})
public class EntityRentalGame {
    @JsonProperty("entity_id")
    public String EntityId = "";

    @JsonProperty("name")
    public String Name = "";

    @JsonProperty("server_name")
    public String ServerName = "";

    @JsonProperty("visibility")
    public int Visibility = 0;

    @JsonProperty("has_pwd")
    public String HasPassword = "";

    @JsonProperty("server_type")
    public int ServerType = 0;

    @JsonProperty("status")
    public int Status = 0;

    @JsonProperty("capacity")
    @Unsigned(32)
    public long Capacity;

    @JsonProperty("mc_version")
    public String McVersion = "";

    @JsonProperty("owner_id")
    public long OwnerId;

    @JsonProperty("player_count")
    @Unsigned(32)
    public long PlayerCount;

    @JsonProperty("image_url")
    @JsonFormat(with=JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    public java.util.List<String> ImageUrl;

    @JsonProperty("world_id")
    public String WorldId = "";

    @JsonProperty("min_level")
    public String MinLevel = "";

    @JsonProperty("pvp")
    public boolean IsPvpEnabled;

    @JsonProperty("like_num")
    @Unsigned(32)
    public long LikeCount;

    @JsonProperty("icon_index")
    @Unsigned(32)
    public long IconIndex;

    @JsonProperty("offset")
    public String Offset;

    /** Known enum value, or null when the raw code is undeclared. */
    public codexus.cipher.entities.wpflauncher.rentalgame.EnumVisibilityStatus getVisibility() {
        return codexus.cipher.entities.wpflauncher.rentalgame.EnumVisibilityStatus.fromValue(Visibility);
    }

    public void setVisibility(codexus.cipher.entities.wpflauncher.rentalgame.EnumVisibilityStatus value) {
        Visibility = java.util.Objects.requireNonNull(value).value();
    }

    /** Known enum value, or null when the raw code is undeclared. */
    public codexus.cipher.entities.wpflauncher.rentalgame.EnumServerType getServerType() {
        return codexus.cipher.entities.wpflauncher.rentalgame.EnumServerType.fromValue(ServerType);
    }

    public void setServerType(codexus.cipher.entities.wpflauncher.rentalgame.EnumServerType value) {
        ServerType = java.util.Objects.requireNonNull(value).value();
    }

    /** Known enum value, or null when the raw code is undeclared. */
    public codexus.cipher.entities.wpflauncher.rentalgame.EnumServerStatus getStatus() {
        return codexus.cipher.entities.wpflauncher.rentalgame.EnumServerStatus.fromValue(Status);
    }

    public void setStatus(codexus.cipher.entities.wpflauncher.rentalgame.EnumServerStatus value) {
        Status = java.util.Objects.requireNonNull(value).value();
    }

}
