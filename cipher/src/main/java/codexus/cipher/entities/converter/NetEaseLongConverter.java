package codexus.cipher.entities.converter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import java.io.IOException;

/** Reads an account ID supplied as either an integer or a decimal string. */
public final class NetEaseLongConverter extends JsonDeserializer<Long> {
    @Override public Long deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (parser.currentToken() == JsonToken.VALUE_NUMBER_INT) return parser.getLongValue();
        if (parser.currentToken() == JsonToken.VALUE_STRING) {
            try { return Long.parseLong(parser.getText().strip()); }
            catch (NumberFormatException error) { throw JsonMappingException.from(parser, "Invalid Int64", error); }
        }
        throw JsonMappingException.from(parser, "Expected Int64 or string");
    }

    @Override public Long getNullValue(DeserializationContext context) throws JsonMappingException {
        throw JsonMappingException.from(context, "Cannot convert null to Int64");
    }
}
