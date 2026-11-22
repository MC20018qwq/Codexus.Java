package codexus.heypixel.plugin.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.*;
public class EnumMetadata {
    @JsonProperty("enumName") public String EnumName="";
    @JsonProperty("fields") public Map<String,EnumFieldMetadata> Fields=new LinkedHashMap<>();
}
