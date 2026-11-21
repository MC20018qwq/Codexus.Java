package codexus.cipher.entities.converter;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import java.io.IOException;
public final class NetEaseStringConverter extends JsonDeserializer<Integer> {
    @Override public Integer deserialize(JsonParser p,DeserializationContext c)throws IOException {
        try {
            if(p.currentToken()==JsonToken.VALUE_STRING)return Integer.parseInt(p.getText().strip());
            if(p.currentToken()==JsonToken.VALUE_NUMBER_INT)return p.getIntValue();
            throw new IllegalArgumentException("Expected Int32 or string");
        } catch(IllegalArgumentException e){throw JsonMappingException.from(p,"Invalid Int32",e);}
    }
    @Override public Integer getNullValue(DeserializationContext c)throws JsonMappingException {throw JsonMappingException.from(c,"Cannot convert null to Int32");}
}
