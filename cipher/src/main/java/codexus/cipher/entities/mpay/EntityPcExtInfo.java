package codexus.cipher.entities.mpay;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/MPay/EntityPcExtInfo.cs. */
@JsonPropertyOrder({"src_jf_game_id","src_app_channel","from_game_id","src_client_type","src_udid","src_sdk_version","src_pay_channel","src_client_ip","extra_unisdk_data"})
public class EntityPcExtInfo {
    @JsonProperty("src_jf_game_id")
    public String SrcJfGameId = "";

    @JsonProperty("src_app_channel")
    public String SrcAppChannel = "";

    @JsonProperty("from_game_id")
    public String FromGameId = "";

    @JsonProperty("src_client_type")
    public int SrcClientType;

    @JsonProperty("src_udid")
    public String SrcUdid = "";

    @JsonProperty("src_sdk_version")
    public String SrcSdkVersion = "";

    @JsonProperty("src_pay_channel")
    public String SrcPayChannel = "";

    @JsonProperty("src_client_ip")
    public String SrcClientIp = "";

    @JsonProperty("extra_unisdk_data")
    public String ExtraUnisdkData = "";

}
