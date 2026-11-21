package codexus.cipher.entities.wpflauncher.netgame.texture;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/NetGame/Texture/EntityUserGameTexture.cs. */
@JsonPropertyOrder({"entity_id","game_type","skin_type","skin_id","skin_mode","client_type"})
public class EntityUserGameTexture {
    @JsonProperty("entity_id")
    public String EntityId = "";

    @JsonProperty("game_type")
    @codexus.csharpsup.DotNetStringEnum.Type(codexus.cipher.entities.wpflauncher.netgame.texture.EnumGType.class)
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = codexus.csharpsup.DotNetStringEnum.Serializer.class)
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = codexus.csharpsup.DotNetStringEnum.Deserializer.class)
    public int GameType = 0;

    @JsonProperty("skin_type")
    @codexus.csharpsup.DotNetStringEnum.Type(codexus.cipher.entities.wpflauncher.netgame.texture.EnumTextureType.class)
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = codexus.csharpsup.DotNetStringEnum.Serializer.class)
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = codexus.csharpsup.DotNetStringEnum.Deserializer.class)
    public int SkinType = 0;

    @JsonProperty("skin_id")
    public String SkinId = "";

    @JsonProperty("skin_mode")
    public int SkinMode;

    @JsonProperty("client_type")
    @codexus.csharpsup.DotNetStringEnum.Type(codexus.cipher.entities.EnumGameClientType.class)
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = codexus.csharpsup.DotNetStringEnum.Serializer.class)
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = codexus.csharpsup.DotNetStringEnum.Deserializer.class)
    public int ClientType = 0;

    /** Known enum value, or null when the raw code is undeclared. */
    public codexus.cipher.entities.wpflauncher.netgame.texture.EnumGType getGameType() {
        return codexus.cipher.entities.wpflauncher.netgame.texture.EnumGType.fromValue(GameType);
    }

    public void setGameType(codexus.cipher.entities.wpflauncher.netgame.texture.EnumGType value) {
        GameType = java.util.Objects.requireNonNull(value).value();
    }

    /** Known enum value, or null when the raw code is undeclared. */
    public codexus.cipher.entities.wpflauncher.netgame.texture.EnumTextureType getSkinType() {
        return codexus.cipher.entities.wpflauncher.netgame.texture.EnumTextureType.fromValue(SkinType);
    }

    public void setSkinType(codexus.cipher.entities.wpflauncher.netgame.texture.EnumTextureType value) {
        SkinType = java.util.Objects.requireNonNull(value).value();
    }

    /** Known enum value, or null when the raw code is undeclared. */
    public codexus.cipher.entities.EnumGameClientType getClientType() {
        return codexus.cipher.entities.EnumGameClientType.fromValue(ClientType);
    }

    public void setClientType(codexus.cipher.entities.EnumGameClientType value) {
        ClientType = java.util.Objects.requireNonNull(value).value();
    }

}
