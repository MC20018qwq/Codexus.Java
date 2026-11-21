package codexus.cipher.entities.wpflauncher.netgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/NetGame/EntityComponentDownloadInfoResponse.cs. */
@JsonPropertyOrder({"entity_id","item_id","user_id","itype","mtypeid","stypeid","sub_entities","sub_mod_list"})
public class EntityComponentDownloadInfoResponse {
    @JsonProperty("entity_id")
    @DotNetRequired
    public String EntityId;

    @JsonProperty("item_id")
    @DotNetRequired
    public String ItemId;

    @JsonProperty("user_id")
    @DotNetRequired
    public String UserId;

    @JsonProperty("itype")
    @DotNetRequired
    public int IType;

    @JsonProperty("mtypeid")
    @DotNetRequired
    public int MTypeId;

    @JsonProperty("stypeid")
    @DotNetRequired
    public int STypeId;

    @JsonProperty("sub_entities")
    @DotNetRequired
    public java.util.List<codexus.cipher.entities.wpflauncher.netgame.EntityComponentDownloadInfoResponseSub> SubEntities;

    @JsonProperty("sub_mod_list")
    @DotNetRequired
    public java.util.List<java.math.BigInteger> SubModList;

}
