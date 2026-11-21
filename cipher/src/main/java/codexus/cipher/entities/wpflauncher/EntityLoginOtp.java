package codexus.cipher.entities.wpflauncher;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/EntityLoginOtp.cs. */
@JsonPropertyOrder({"otp_token","aid","lock_time","open_otp"})
public class EntityLoginOtp {
    @JsonProperty("otp_token")
    public String OtpToken = "";

    @JsonProperty("aid")
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = codexus.cipher.entities.converter.NetEaseStringConverter.class)
    public int Aid;

    @JsonProperty("lock_time")
    public int LockTime;

    @JsonProperty("open_otp")
    public int OpenOtp;

}
