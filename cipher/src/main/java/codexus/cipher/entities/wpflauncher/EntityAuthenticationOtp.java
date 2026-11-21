package codexus.cipher.entities.wpflauncher;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/EntityAuthenticationOtp.cs. */
@JsonPropertyOrder({"entity_id","account","token","sead","hasMessage","aid","sdkuid","access_token","unisdk_login_json","verify_status","hasGmail","is_register","env","last_server_up_time","min_engine_version","min_patch_version"})
public class EntityAuthenticationOtp {
    @JsonProperty("entity_id")
    public String EntityId = "";

    @JsonProperty("account")
    public String Account = "";

    @JsonProperty("token")
    public String Token = "";

    @JsonProperty("sead")
    public String Sead = "";

    @JsonProperty("hasMessage")
    public boolean HasMessage;

    @JsonProperty("aid")
    public String Aid = "";

    @JsonProperty("sdkuid")
    public String SdkUid = "";

    @JsonProperty("access_token")
    public String AccessToken = "";

    @JsonProperty("unisdk_login_json")
    public String UniSdkLoginJson = "";

    @JsonProperty("verify_status")
    public int VerifyStatus;

    @JsonProperty("hasGmail")
    public boolean HasGmail;

    @JsonProperty("is_register")
    public boolean IsRegister;

    @JsonProperty("env")
    public String Env = "";

    @JsonProperty("last_server_up_time")
    public long LastServerUpTime;

    @JsonProperty("min_engine_version")
    public String MinEngineVersion = "";

    @JsonProperty("min_patch_version")
    public String MinPatchVersion = "";

}
