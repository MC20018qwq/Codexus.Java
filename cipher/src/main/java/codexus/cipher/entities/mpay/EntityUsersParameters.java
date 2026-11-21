package codexus.cipher.entities.mpay;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/MPay/EntityUsersParameters.cs. */
@JsonPropertyOrder({"password","unique_id","username"})
public class EntityUsersParameters {
    @JsonProperty("password")
    public String Password = "";

    @JsonProperty("unique_id")
    public String Unique = "";

    @JsonProperty("username")
    public String Username = "";

}
