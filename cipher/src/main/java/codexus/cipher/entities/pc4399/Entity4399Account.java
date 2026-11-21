package codexus.cipher.entities.pc4399;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/Pc4399/Entity4399Account.cs. */
@JsonPropertyOrder({"account","password"})
public class Entity4399Account {
    @JsonProperty("account")
    public String Account = "";

    @JsonProperty("password")
    public String Password = "";

    @Override public boolean equals(Object other) {return other instanceof Entity4399Account o && java.util.Objects.equals(Account,o.Account) && java.util.Objects.equals(Password,o.Password);}
    @Override public int hashCode() {return java.util.Objects.hash(Account,Password);}
    public Entity4399Account clone() {Entity4399Account c=new Entity4399Account();c.Account=Account;c.Password=Password;return c;}
}
