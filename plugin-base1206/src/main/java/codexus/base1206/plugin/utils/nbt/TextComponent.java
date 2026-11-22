package codexus.base1206.plugin.utils.nbt;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import codexus.csharpsup.DotNetJson;
public class TextComponent {
    @JsonIgnore public byte[] RawData;
    @JsonProperty("text") public String Text="";
    @JsonProperty("color") public String Color="";
    public String toJson(){return DotNetJson.serialize(this);}
}
