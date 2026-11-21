package codexus.cipher.entities.wpflauncher;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/EntityAuthenticationData.cs. */
@JsonPropertyOrder({"sa_data","sauth_json","version","sdkuid","aid","hasMessage","hasGmail","otp_token","otp_pwd","lock_time","env","min_engine_version","min_patch_version","verify_status","unisdk_login_json","token","is_register","entity_id"})
public class EntityAuthenticationData {
    @JsonProperty("sa_data")
    @DotNetRequired
    public String SaData;

    @JsonProperty("sauth_json")
    @DotNetRequired
    public String AuthJson;

    @JsonProperty("version")
    @DotNetRequired
    public codexus.cipher.entities.wpflauncher.EntityAuthenticationVersion Version;

    @JsonProperty("sdkuid")
    public String SdkUid;

    @JsonProperty("aid")
    @DotNetRequired
    public String Aid;

    @JsonProperty("hasMessage")
    public boolean HasMessage;

    @JsonProperty("hasGmail")
    public boolean HasGmail;

    @JsonProperty("otp_token")
    @DotNetRequired
    public String OtpToken;

    @JsonProperty("otp_pwd")
    public String OtpPwd;

    @JsonProperty("lock_time")
    public int LockTime;

    @JsonProperty("env")
    public String Env;

    @JsonProperty("min_engine_version")
    public String MinEngineVersion;

    @JsonProperty("min_patch_version")
    public String MinPatchVersion;

    @JsonProperty("verify_status")
    public int VerifyStatus;

    @JsonProperty("unisdk_login_json")
    public String UniSdkLoginJson;

    @JsonProperty("token")
    public String Token;

    @JsonProperty("is_register")
    public boolean IsRegister = true;

    @JsonProperty("entity_id")
    public String EntityId;

}
