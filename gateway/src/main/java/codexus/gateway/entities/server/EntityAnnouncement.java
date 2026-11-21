package codexus.gateway.entities.server;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Gateway/Codexus/Gateway/Entities/Server/EntityAnnouncement.cs. */
@JsonPropertyOrder({"id","title","content","date","author"})
public class EntityAnnouncement {
    @JsonProperty("id")
    @DotNetRequired
    public int Id;

    @JsonProperty("title")
    @DotNetRequired
    public String Title;

    @JsonProperty("content")
    @DotNetRequired
    public String Content;

    @JsonProperty("date")
    @DotNetRequired
    public String Date;

    @JsonProperty("author")
    @DotNetRequired
    public String Author;

}
