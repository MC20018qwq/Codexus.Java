package codexus.development.sdk.utils;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.*;

public final class JsonElementExtensions {
    private JsonElementExtensions() { }
    public static Object getObject(JsonNode element) {
        if (element.isTextual()) return element.textValue();
        if (element.isNumber()) {
            if (element.isIntegralNumber() && element.canConvertToInt()) return element.intValue();
            if (element.isIntegralNumber() && element.canConvertToLong()) return element.longValue();
            return element.doubleValue();
        }
        if (element.isBoolean()) return element.booleanValue();
        if (element.isNull()) return null;
        if (element.isArray()) {
            var result = new ArrayList<Object>();
            element.forEach(item -> result.add(getObject(item)));
            return result;
        }
        if (element.isObject()) {
            var result = new LinkedHashMap<String, Object>();
            element.fields().forEachRemaining(item -> result.put(item.getKey(), getObject(item.getValue())));
            return result;
        }
        return element;
    }
}
