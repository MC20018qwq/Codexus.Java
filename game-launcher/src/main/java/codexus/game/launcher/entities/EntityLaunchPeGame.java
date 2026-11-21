package codexus.game.launcher.entities;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Game.Launcher/Codexus/Game/Launcher/Entities/EntityLaunchPeGame.cs. */
@JsonPropertyOrder({"game_name","game_id","role_name","user_id","client_type","game_type","is_domain_game","launch_type","launch_path","access_token","server_ip","server_port","skin_path","item_ids"})
public class EntityLaunchPeGame {
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

    @JsonProperty("is_domain_game")
    public boolean IsDomainGame;

    @JsonProperty("launch_type")
    public int LaunchType = 0;

    @JsonProperty("launch_path")
    public String LaunchPath;

    @JsonProperty("access_token")
    public String AccessToken = "";

    @JsonProperty("server_ip")
    public String ServerIp = "";

    @JsonProperty("server_port")
    public int ServerPort;

    @JsonProperty("skin_path")
    public String SkinPath = "";

    @JsonProperty("item_ids")
    public java.util.List<String> ItemIds;

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

    /** Known enum value, or null when the raw code is undeclared. */
    public codexus.game.launcher.entities.EnumLaunchType getLaunchType() {
        return codexus.game.launcher.entities.EnumLaunchType.fromValue(LaunchType);
    }

    public void setLaunchType(codexus.game.launcher.entities.EnumLaunchType value) {
        LaunchType = java.util.Objects.requireNonNull(value).value();
    }

}
