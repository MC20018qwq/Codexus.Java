package codexus.cipher.entities.connection;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/Connection/YggdrasilServer.cs. */
@JsonPropertyOrder({"IP","Port"})
public class YggdrasilServer {
    @JsonProperty("IP")
    public String Ip = "";

    @JsonProperty("Port")
    public int Port;

}
