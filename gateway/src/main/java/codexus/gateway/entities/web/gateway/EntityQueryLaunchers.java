package codexus.gateway.entities.web.gateway;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Gateway/Codexus/Gateway/Entities/Web/Gateway/EntityQueryLaunchers.cs. */
@JsonPropertyOrder({"id","name","role","server","version","status","progress","process_id"})
public class EntityQueryLaunchers {
    @JsonProperty("id")
    @DotNetRequired
    public String Id;

    @JsonProperty("name")
    @DotNetRequired
    public java.util.UUID Name = codexus.csharpsup.DotNetGuid.EMPTY;

    @JsonProperty("role")
    @DotNetRequired
    public String Role;

    @JsonProperty("server")
    @DotNetRequired
    public String Server;

    @JsonProperty("version")
    @DotNetRequired
    public String Version;

    @JsonProperty("status")
    @DotNetRequired
    public String StatusMessage;

    @JsonProperty("progress")
    @DotNetRequired
    public int Progress;

    @JsonProperty("process_id")
    @DotNetRequired
    public int ProcessId;

}
