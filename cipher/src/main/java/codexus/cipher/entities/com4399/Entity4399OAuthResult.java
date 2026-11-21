package codexus.cipher.entities.com4399;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/Com4399/Entity4399OAuthResult.cs. */
@JsonPropertyOrder({"login_url","login_url_backup","login_url_phone","login_url_backup_phone"})
public class Entity4399OAuthResult {
    @JsonProperty("login_url")
    public String LoginUrl = "";

    @JsonProperty("login_url_backup")
    public String LoginUrlBackup = "";

    @JsonProperty("login_url_phone")
    public String LoginUrlPhone = "";

    @JsonProperty("login_url_backup_phone")
    public String LoginUrlBackupPhone = "";

}
