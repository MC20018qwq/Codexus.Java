package codexus.cipher.entities.wpflauncher.netgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/NetGame/EntityNetGameServerAddress.cs. */
@JsonPropertyOrder({"entity_id","isp_enable","ip","port","game_status","announcement","in_whitelist"})
public class EntityNetGameServerAddress {
    @JsonProperty("entity_id")
    public String EntityId = "";

    @JsonProperty("isp_enable")
    public boolean IspEnable = true;

    @JsonProperty("ip")
    public String Ip = "";

    @JsonProperty("port")
    public int Port;

    @JsonProperty("game_status")
    public int GameStatus;

    @JsonProperty("announcement")
    public String Announcement = "";

    @JsonProperty("in_whitelist")
    public boolean InWhitelist;

}
