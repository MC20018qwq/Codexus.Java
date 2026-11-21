package codexus.cipher.entities.g79.netgame;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/G79/NetGame/EntityNetGameRequest.cs. */
@JsonPropertyOrder({"version","channel_id"})
public class EntityNetGameRequest {
    @JsonProperty("version")
    @DotNetRequired
    public String Version;

    @JsonProperty("channel_id")
    @DotNetRequired
    public int ChannelId;

}
