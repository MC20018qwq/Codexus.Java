package codexus.cipher.entities.mpay;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Cipher/Codexus/Cipher/Entities/MPay/EntitySmsTicket.cs. */
@JsonPropertyOrder({"guide_text","related_emails","ticket","related_accounts"})
public class EntitySmsTicket {
    @JsonProperty("guide_text")
    public String GuideText = "";

    @JsonProperty("related_emails")
    public String[] RelatedEmails = new String[0];

    @JsonProperty("ticket")
    public String Ticket = "";

    @JsonProperty("related_accounts")
    public String[] RelatedAccounts = new String[0];

}
