package codexus.cipher.entities.wpflauncher.rpc;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/RPC/EntityCheckPlayerMessage.cs. */
@JsonPropertyOrder({"a","b"})
public class EntityCheckPlayerMessage {
    @JsonProperty("a")
    @DotNetRequired
    public int Length;

    @JsonProperty("b")
    @DotNetRequired
    public String Message;

}
