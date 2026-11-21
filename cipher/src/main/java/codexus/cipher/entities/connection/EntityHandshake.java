package codexus.cipher.entities.connection;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/Connection/EntityHandshake.cs. */
@JsonPropertyOrder({"handshakeBody"})
public class EntityHandshake {
    @JsonProperty("handshakeBody")
    @DotNetRequired
    public String HandshakeBody;

}
