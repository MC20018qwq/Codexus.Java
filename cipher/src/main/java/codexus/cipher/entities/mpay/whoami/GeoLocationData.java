package codexus.cipher.entities.mpay.whoami;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/MPay/WhoAmi/GeoLocationData.cs. */
@JsonPropertyOrder({"code_1","code_2","code_3","code_4","isp","ip"})
public class GeoLocationData {
    @JsonProperty("code_1")
    public codexus.cipher.entities.mpay.whoami.Code1 Code1;

    @JsonProperty("code_2")
    public codexus.cipher.entities.mpay.whoami.Code2 Code2;

    @JsonProperty("code_3")
    public codexus.cipher.entities.mpay.whoami.Code3 Code3;

    @JsonProperty("code_4")
    public codexus.cipher.entities.mpay.whoami.Code4 Code4;

    @JsonProperty("isp")
    public codexus.cipher.entities.mpay.whoami.Isp Isp;

    @JsonProperty("ip")
    public String Ip;

}
