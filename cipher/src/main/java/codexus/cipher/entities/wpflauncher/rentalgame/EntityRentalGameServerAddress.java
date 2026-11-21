package codexus.cipher.entities.wpflauncher.rentalgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/RentalGame/EntityRentalGameServerAddress.cs. */
@JsonPropertyOrder({"entity_id","mcserver_host","mcserver_port","state","cmcc_mcserver_host","cmcc_mcserver_port","ctcc_mcserver_host","ctcc_mcserver_port","cucc_mcserver_host","cucc_mcserver_port","isp_enable"})
public class EntityRentalGameServerAddress {
    @JsonProperty("entity_id")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = codexus.cipher.utils.JsonConventer.StringFromNumberOrStringConverter.class)
    public String EntityId = "";

    @JsonProperty("mcserver_host")
    public String McServerHost = "";

    @JsonProperty("mcserver_port")
    @Unsigned(16)
    public int McServerPort;

    @JsonProperty("state")
    public int State = 0;

    @JsonProperty("cmcc_mcserver_host")
    public String CmccMcServerHost = "";

    @JsonProperty("cmcc_mcserver_port")
    public int CmccMcServerPort;

    @JsonProperty("ctcc_mcserver_host")
    public String CtccMcServerHost = "";

    @JsonProperty("ctcc_mcserver_port")
    public int CtccMcServerPort;

    @JsonProperty("cucc_mcserver_host")
    public String CuccMcServerHost = "";

    @JsonProperty("cucc_mcserver_port")
    public int CussMcServerPort;

    @JsonProperty("isp_enable")
    public boolean IspEnable;

    /** Known enum value, or null when the raw code is undeclared. */
    public codexus.cipher.entities.wpflauncher.rentalgame.EnumServerStatus getState() {
        return codexus.cipher.entities.wpflauncher.rentalgame.EnumServerStatus.fromValue(State);
    }

    public void setState(codexus.cipher.entities.wpflauncher.rentalgame.EnumServerStatus value) {
        State = java.util.Objects.requireNonNull(value).value();
    }

}
