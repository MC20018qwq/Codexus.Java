package codexus.cipher.entities.mpay;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/MPay/EntityVerifyStatus.cs. */
@JsonPropertyOrder({"need_sms","need_email","need_passwd","need_real_name"})
public class EntityVerifyStatus {
    @JsonProperty("need_sms")
    public int NeedSms;

    @JsonProperty("need_email")
    public int NeedEmail;

    @JsonProperty("need_passwd")
    public int NeedPasswd;

    @JsonProperty("need_real_name")
    public int NeedRealName;

}
