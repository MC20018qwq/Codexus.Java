package codexus.gateway.entities;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Gateway/Codexus/Gateway/Entities/Entity.cs. */
@JsonPropertyOrder({"identify","type","payload","sign"})
public class Entity {
    @JsonProperty("identify")
    public String Identify;

    @JsonProperty("type")
    public String Type;

    @JsonProperty("payload")
    public String Payload;

    @JsonProperty("sign")
    public String Sign;

    public Entity() {}
    public Entity(String type, String payload) {
        Type = java.util.Objects.requireNonNull(type, "type"); Payload = java.util.Objects.requireNonNull(payload, "payload");
    }
    public void prepareSignatureAndIdentify(String identify) {
        Identify=identify; if(Payload==null)throw new IllegalStateException("Cannot generate signature:  Payload is null.");
        Sign=codexus.gateway.extensions.StringExtensions.toSha256(Payload);
    }
}
