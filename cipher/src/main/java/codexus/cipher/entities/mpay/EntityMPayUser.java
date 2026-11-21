package codexus.cipher.entities.mpay;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/MPay/EntityMPayUser.cs. */
@JsonPropertyOrder({"ext_access_token","realname_verify_status","login_channel","realname_status","related_login_status","need_mask","mobile_bind_status","mask_related_mobile","display_username","token","client_username","avatar","need_aas","login_type","nickname","id","pc_ext_info"})
public class EntityMPayUser {
    @JsonProperty("ext_access_token")
    public String ExtAccessToken = "";

    @JsonProperty("realname_verify_status")
    public int RealNameVerifyStatus;

    @JsonProperty("login_channel")
    public String LoginChannel = "";

    @JsonProperty("realname_status")
    public int RealNameStatus;

    @JsonProperty("related_login_status")
    public int RelatedLoginStatus;

    @JsonProperty("need_mask")
    public boolean NeedMask;

    @JsonProperty("mobile_bind_status")
    public int MobileBindStatus;

    @JsonProperty("mask_related_mobile")
    public String MaskRelatedMobile = "";

    @JsonProperty("display_username")
    public String DisplayUsername = "";

    @JsonProperty("token")
    public String Token = "";

    @JsonProperty("client_username")
    public String ClientUsername = "";

    @JsonProperty("avatar")
    public String Avatar = "";

    @JsonProperty("need_aas")
    public boolean NeedAas;

    @JsonProperty("login_type")
    public int LoginType;

    @JsonProperty("nickname")
    public String Nickname = "";

    @JsonProperty("id")
    public String Id = "";

    @JsonProperty("pc_ext_info")
    public codexus.cipher.entities.mpay.EntityPcExtInfo EntityPcExtInfo = new codexus.cipher.entities.mpay.EntityPcExtInfo();

}
