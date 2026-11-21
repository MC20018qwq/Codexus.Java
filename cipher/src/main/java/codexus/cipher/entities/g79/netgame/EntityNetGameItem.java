package codexus.cipher.entities.g79.netgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/G79/NetGame/EntityNetGameItem.cs. */
@JsonPropertyOrder({"item_id","res_name","brief","tag_names","title_image_url","new_recommend","new_entrance_recommend","new_recommend_time","order","is_spigot","stars","entity_id","online_num","pic_url_list","is_access_by_uid","opening_hour","sort_description","is_show_online_count","sort","is_fellow","developer_id","friend_play_num","week_play_num","recommend_sort_num","total_play_num","create_time","running_status"})
public class EntityNetGameItem {
    @JsonProperty("item_id")
    @DotNetRequired
    public String ItemId;

    @JsonProperty("res_name")
    @DotNetRequired
    public String ResName;

    @JsonProperty("brief")
    @DotNetRequired
    public String Brief;

    @JsonProperty("tag_names")
    @DotNetRequired
    public java.util.List<String> TagNames;

    @JsonProperty("title_image_url")
    @DotNetRequired
    public String TitleImageUrl;

    @JsonProperty("new_recommend")
    @DotNetRequired
    public int NewRecommend;

    @JsonProperty("new_entrance_recommend")
    @DotNetRequired
    public int NewEntranceRecommend;

    @JsonProperty("new_recommend_time")
    @DotNetRequired
    public int NewRecommendTime;

    @JsonProperty("order")
    @DotNetRequired
    public String Order;

    @JsonProperty("is_spigot")
    @DotNetRequired
    public int IsSpigot;

    @JsonProperty("stars")
    @DotNetRequired
    public float Stars;

    @JsonProperty("entity_id")
    @DotNetRequired
    public String EntityId;

    @JsonProperty("online_num")
    @DotNetRequired
    public String OnlineNum;

    @JsonProperty("pic_url_list")
    @DotNetRequired
    public java.util.List<String> PicUrlList;

    @JsonProperty("is_access_by_uid")
    @DotNetRequired
    public int IsAccessByUid;

    @JsonProperty("opening_hour")
    @DotNetRequired
    public String OpeningHour;

    @JsonProperty("sort_description")
    @DotNetRequired
    public String SortDescription;

    @JsonProperty("is_show_online_count")
    @DotNetRequired
    public int IsShowOnlineCount;

    @JsonProperty("sort")
    @DotNetRequired
    public int Sort;

    @JsonProperty("is_fellow")
    @DotNetRequired
    public int IsFellow;

    @JsonProperty("developer_id")
    @DotNetRequired
    public int DeveloperId;

    @JsonProperty("friend_play_num")
    @DotNetRequired
    public int FriendPlayNum;

    @JsonProperty("week_play_num")
    @DotNetRequired
    public int WeekPlayNum;

    @JsonProperty("recommend_sort_num")
    @DotNetRequired
    public int RecommendSortNum;

    @JsonProperty("total_play_num")
    @DotNetRequired
    public int TotalPlayNum;

    @JsonProperty("create_time")
    @DotNetRequired
    public int CreateTime;

    @JsonProperty("running_status")
    @DotNetRequired
    public String RunningStatus;

}
