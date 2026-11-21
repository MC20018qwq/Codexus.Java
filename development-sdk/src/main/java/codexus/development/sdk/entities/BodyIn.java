package codexus.development.sdk.entities;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Development.SDK/Codexus/Development/SDK/Entities/BodyIn.cs. */
@JsonPropertyOrder({"body"})
public class BodyIn {
    @JsonProperty("body")
    @DotNetRequired
    public String Body;

}
