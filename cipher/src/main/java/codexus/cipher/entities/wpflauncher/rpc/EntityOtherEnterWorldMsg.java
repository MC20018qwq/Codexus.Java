package codexus.cipher.entities.wpflauncher.rpc;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/WPFLauncher/RPC/EntityOtherEnterWorldMsg.cs. */
@JsonPropertyOrder({"id","len","name","len1","uuid"})
public class EntityOtherEnterWorldMsg {
    @JsonProperty("id")
    public short Id;

    @JsonProperty("len")
    @Unsigned(16)
    public int Length;

    @JsonProperty("name")
    public String Name = "";

    @JsonProperty("len1")
    @Unsigned(16)
    public int UuidLength;

    @JsonProperty("uuid")
    public String Uuid = "";

}
