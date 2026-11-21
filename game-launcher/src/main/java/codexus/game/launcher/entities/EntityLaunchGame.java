package codexus.game.launcher.entities;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Game.Launcher/Codexus/Game/Launcher/Entities/EntityLaunchGame.cs. */
@JsonPropertyOrder({"game_name","game_id","role_name","user_id","client_type","game_type","game_version_id","game_version","access_token","server_ip","server_port","max_game_memory","load_core_mods"})
public class EntityLaunchGame {
    @JsonProperty("game_name")
    public String GameName = "";

    @JsonProperty("game_id")
    public String GameId = "";

    @JsonProperty("role_name")
    public String RoleName = "";

    @JsonProperty("user_id")
    public String UserId = "";

    @JsonProperty("client_type")
    public int ClientType = 0;

    @JsonProperty("game_type")
    public int GameType = 0;

    @JsonProperty("game_version_id")
    public int GameVersionId;

    @JsonProperty("game_version")
    public String GameVersion = "";

    @JsonProperty("access_token")
    public String AccessToken = "";

    @JsonProperty("server_ip")
    public String ServerIp = "";

    @JsonProperty("server_port")
    public int ServerPort;

    @JsonProperty("max_game_memory")
    public int MaxGameMemory;

    @JsonProperty("load_core_mods")
    public boolean LoadCoreMods;

    /** Known enum value, or null when the raw code is undeclared. */
    public codexus.cipher.entities.EnumGameClientType getClientType() {
        return codexus.cipher.entities.EnumGameClientType.fromValue(ClientType);
    }

    public void setClientType(codexus.cipher.entities.EnumGameClientType value) {
        ClientType = java.util.Objects.requireNonNull(value).value();
    }

    /** Known enum value, or null when the raw code is undeclared. */
    public codexus.cipher.entities.wpflauncher.netgame.texture.EnumGType getGameType() {
        return codexus.cipher.entities.wpflauncher.netgame.texture.EnumGType.fromValue(GameType);
    }

    public void setGameType(codexus.cipher.entities.wpflauncher.netgame.texture.EnumGType value) {
        GameType = java.util.Objects.requireNonNull(value).value();
    }

}
