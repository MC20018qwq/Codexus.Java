package codexus.cipher.entities.wpflauncher.netgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/NetGame/EntityQueryNetGameDetailItem.cs. */
@JsonPropertyOrder({"entity_id","name","brief_image_urls","detail_description","developer_name","developer_urs","publish_time","video_info_list","mc_version_list","server_address","server_port"})
public class EntityQueryNetGameDetailItem {
    @JsonProperty("entity_id")
    public String EntityId = "";

    @JsonProperty("name")
    public String Name = "";

    @JsonProperty("brief_image_urls")
    public String[] BriefImageUrls = new String[0];

    @JsonProperty("detail_description")
    public String DetailDescription = "";

    @JsonProperty("developer_name")
    public String DeveloperName = "";

    @JsonProperty("developer_urs")
    public String DeveloperUrs = "";

    @JsonProperty("publish_time")
    public int PublishTime;

    @JsonProperty("video_info_list")
    public codexus.cipher.entities.wpflauncher.netgame.EntityDetailsVideo[] VideoInfoList = new codexus.cipher.entities.wpflauncher.netgame.EntityDetailsVideo[0];

    @JsonProperty("mc_version_list")
    public codexus.cipher.entities.wpflauncher.netgame.EntityMcVersion[] McVersionList = new codexus.cipher.entities.wpflauncher.netgame.EntityMcVersion[0];

    @JsonProperty("server_address")
    public String ServerAddress = "";

    @JsonProperty("server_port")
    public int ServerPort;

}
