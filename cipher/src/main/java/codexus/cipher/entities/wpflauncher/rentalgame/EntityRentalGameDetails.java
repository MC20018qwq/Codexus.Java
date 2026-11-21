package codexus.cipher.entities.wpflauncher.rentalgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/RentalGame/EntityRentalGameDetails.cs. */
@JsonPropertyOrder({"entity_id","owner_id","name","brief_summary","icon_index","begin_time","mc_version","capacity","world_id","player_count","image_url","status","visibility","has_pwd","server_type","active_components","update_active_components","server_ip","server_port"})
public class EntityRentalGameDetails {
    @JsonProperty("entity_id")
    public String EntityId = "";

    @JsonProperty("owner_id")
    public String OwnerId = "";

    @JsonProperty("name")
    public String Name = "";

    @JsonProperty("brief_summary")
    public String BriefSummary = "";

    @JsonProperty("icon_index")
    @Unsigned(32)
    public long IconIndex;

    @JsonProperty("begin_time")
    @Unsigned(64)
    public java.math.BigInteger BeginTime = java.math.BigInteger.ZERO;

    @JsonProperty("mc_version")
    public String McVersion = "";

    @JsonProperty("capacity")
    @Unsigned(32)
    public long Capacity;

    @JsonProperty("world_id")
    public String WorldId = "";

    @JsonProperty("player_count")
    @Unsigned(32)
    public long PlayerCount;

    @JsonProperty("image_url")
    public String ImageUrl = "";

    @JsonProperty("status")
    public int Status = 0;

    @JsonProperty("visibility")
    public int Visibility = 0;

    @JsonProperty("has_pwd")
    public String HasPassword = "";

    @JsonProperty("server_type")
    public int ServerType = 0;

    @JsonProperty("active_components")
    public java.util.List<String> ActiveComponents = new java.util.ArrayList<>();

    @JsonProperty("update_active_components")
    public java.util.List<String> UpdateActiveComponents = new java.util.ArrayList<>();

    @JsonProperty("server_ip")
    public String ServerIp;

    @JsonProperty("server_port")
    public Integer ServerPort;

    /** Known enum value, or null when the raw code is undeclared. */
    public codexus.cipher.entities.wpflauncher.rentalgame.EnumServerStatus getStatus() {
        return codexus.cipher.entities.wpflauncher.rentalgame.EnumServerStatus.fromValue(Status);
    }

    public void setStatus(codexus.cipher.entities.wpflauncher.rentalgame.EnumServerStatus value) {
        Status = java.util.Objects.requireNonNull(value).value();
    }

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

}
