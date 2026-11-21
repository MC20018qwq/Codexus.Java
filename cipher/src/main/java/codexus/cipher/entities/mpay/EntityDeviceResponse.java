package codexus.cipher.entities.mpay;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/MPay/EntityDeviceResponse.cs. */
@JsonPropertyOrder({"device"})
public class EntityDeviceResponse {
    @JsonProperty("device")
    public codexus.cipher.entities.mpay.EntityDevice EntityDevice = new codexus.cipher.entities.mpay.EntityDevice();

}
