package codexus.cipher.utils;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import java.io.IOException;
public final class JsonConventer {
    private JsonConventer() {}
    public static final class StringFromNumberOrStringConverter extends JsonDeserializer<String> {
        @Override public String deserialize(JsonParser p,DeserializationContext c)throws IOException {
            if(p.currentToken()==JsonToken.VALUE_NUMBER_INT)return Long.toString(p.getLongValue());
            if(p.currentToken()==JsonToken.VALUE_STRING)return p.getText();
            throw JsonMappingException.from(p,"Unsupported token type for string conversion.");
        }
    }
}
