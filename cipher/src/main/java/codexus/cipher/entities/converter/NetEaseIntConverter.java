package codexus.cipher.entities.converter;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import java.io.IOException;
public final class NetEaseIntConverter extends JsonDeserializer<String> {
    @Override public String deserialize(JsonParser p,DeserializationContext c)throws IOException {
        if(p.currentToken()==JsonToken.VALUE_NUMBER_INT)return Integer.toString(p.getIntValue());
        if(p.currentToken()==JsonToken.VALUE_STRING)return p.getText();
        throw JsonMappingException.from(p,"Expected Int32 or string");
    }
}
