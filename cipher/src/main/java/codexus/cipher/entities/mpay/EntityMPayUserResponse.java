package codexus.cipher.entities.mpay;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/MPay/EntityMPayUserResponse.cs. */
@JsonPropertyOrder({"force_pwd","verify_status","user"})
public class EntityMPayUserResponse {
    @JsonProperty("force_pwd")
    public boolean ForcePwd;

    @JsonProperty("verify_status")
    public codexus.cipher.entities.mpay.EntityVerifyStatus VerifyStatus;

    @JsonProperty("user")
    @DotNetRequired
    public codexus.cipher.entities.mpay.EntityMPayUser User = new codexus.cipher.entities.mpay.EntityMPayUser();

}
