package codexus.cipher.entities.wpflauncher.netgame.skin;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/NetGame/Skin/EntitySkinPurchaseRequest.cs. */
@JsonPropertyOrder({"batch_count","buy_path","diamond","entity_id","item_id","item_level","last_play_time","purchase_time","total_play_time","user_id","receiver_id","coupon_ids","activity_name"})
public class EntitySkinPurchaseRequest {
    @JsonProperty("batch_count")
    @DotNetRequired
    public int BatchCount;

    @JsonProperty("buy_path")
    @DotNetRequired
    public String BuyPath;

    @JsonProperty("diamond")
    @DotNetRequired
    public int Diamond;

    @JsonProperty("entity_id")
    @DotNetRequired
    public int EntityId;

    @JsonProperty("item_id")
    @DotNetRequired
    public String ItemId;

    @JsonProperty("item_level")
    @DotNetRequired
    public int ItemLevel;

    @JsonProperty("last_play_time")
    @DotNetRequired
    public int LastPlayTime;

    @JsonProperty("purchase_time")
    @DotNetRequired
    public int PurchaseTime;

    @JsonProperty("total_play_time")
    @DotNetRequired
    public int TotalPlayTime;

    @JsonProperty("user_id")
    @DotNetRequired
    public String UserId;

    @JsonProperty("receiver_id")
    public String ReceiverId;

    @JsonProperty("coupon_ids")
    public java.util.List<String> CouponIds;

    @JsonProperty("activity_name")
    public String ActivityName;

}
