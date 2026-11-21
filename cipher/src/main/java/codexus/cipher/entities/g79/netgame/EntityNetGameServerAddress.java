package codexus.cipher.entities.g79.netgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/G79/NetGame/EntityNetGameServerAddress.cs. */
@JsonPropertyOrder({"host","port"})
public class EntityNetGameServerAddress {
    @JsonProperty("host")
    @DotNetRequired
    public String Host;

    @JsonProperty("port")
    @DotNetRequired
    public int Port;

}
