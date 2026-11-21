package codexus.development.sdk.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import codexus.csharpsup.DotNetJson;
import java.io.IOException;

public class ObjectConverter extends StdDeserializer<Object> {
    public ObjectConverter() { super(Object.class); }
    @Override public Object deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return JsonElementExtensions.getObject(parser.readValueAsTree());
    }
    public String write(Object value) { return DotNetJson.serialize(value); }
}
