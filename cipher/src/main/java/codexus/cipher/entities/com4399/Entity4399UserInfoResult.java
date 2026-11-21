package codexus.cipher.entities.com4399;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/Com4399/Entity4399UserInfoResult.cs. */
@JsonPropertyOrder({"uid","idcard","reg_time","validateState","bindedphone","idcard_state","realname","username","full_bind_phone","nck","avatar_middle","access_token","state","code","account_type","hello","idcard_editable","id_checked","id_checked_real","phone_bound","activated","vip_info"})
public class Entity4399UserInfoResult {
    @JsonProperty("uid")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = codexus.cipher.entities.converter.NetEaseLongConverter.class)
    public long Uid;

    @JsonProperty("idcard")
    public String IdCard = "";

    @JsonProperty("reg_time")
    public long RegTime;

    @JsonProperty("validateState")
    public int ValidateState;

    @JsonProperty("bindedphone")
    public String BindedPhone = "";

    @JsonProperty("idcard_state")
    public int IdCardState;

    @JsonProperty("realname")
    public String RealName = "";

    @JsonProperty("username")
    public String Username = "";

    @JsonProperty("full_bind_phone")
    public String FullBindPhone = "";

    @JsonProperty("nck")
    public String Nickname = "";

    @JsonProperty("avatar_middle")
    public String AvatarMiddle = "";

    @JsonProperty("access_token")
    public String AccessToken = "";

    @JsonProperty("state")
    public String State = "";

    @JsonProperty("code")
    public String AuthCode = "";

    @JsonProperty("account_type")
    public String AccountType = "";

    @JsonProperty("hello")
    public String WelcomeMessage = "";

    @JsonProperty("idcard_editable")
    public boolean IdCardEditable;

    @JsonProperty("id_checked")
    public boolean IdChecked;

    @JsonProperty("id_checked_real")
    public boolean IdCheckedReal;

    @JsonProperty("phone_bound")
    public int PhoneBound;

    @JsonProperty("activated")
    public boolean Activated;

    @JsonProperty("vip_info")
    public codexus.cipher.entities.com4399.Entity4399VipInfo VipInfo = new codexus.cipher.entities.com4399.Entity4399VipInfo();

}
