package codexus.development.sdk.entities;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Development.SDK/Codexus/Development/SDK/Entities/Property.cs. */
@JsonPropertyOrder({"Name","Value","Signature"})
public class Property {
    @JsonProperty("Name")
    @DotNetRequired
    public String Name;

    @JsonProperty("Value")
    @DotNetRequired
    public String Value;

    @JsonProperty("Signature")
    @DotNetRequired
    public String Signature;

}
