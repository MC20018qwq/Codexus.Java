package codexus.game.launcher.entities;

import com.fasterxml.jackson.annotation.*;
import codexus.csharpsup.*;

/** Port of Codexus.Game.Launcher/Codexus/Game/Launcher/Entities/EntityProgressUpdate.cs. */
@JsonPropertyOrder({"id","percent","message"})
public class EntityProgressUpdate {
    @JsonProperty("id")
    @DotNetRequired
    public java.util.UUID Id = codexus.csharpsup.DotNetGuid.EMPTY;

    @JsonProperty("percent")
    @DotNetRequired
    public int Percent;

    @JsonProperty("message")
    @DotNetRequired
    public String Message;

}
