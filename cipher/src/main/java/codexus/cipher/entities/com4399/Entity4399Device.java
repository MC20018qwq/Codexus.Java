package codexus.cipher.entities.com4399;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/Com4399/Entity4399Device.cs. */
@JsonPropertyOrder({"device-id","device-id-sm","device-udid","device-state"})
public class Entity4399Device {
    @JsonProperty("device-id")
    @DotNetRequired
    public String DeviceIdentifier;

    @JsonProperty("device-id-sm")
    @DotNetRequired
    public String DeviceIdentifierSm;

    @JsonProperty("device-udid")
    @DotNetRequired
    public String DeviceUdid;

    @JsonProperty("device-state")
    public String DeviceState;

}
