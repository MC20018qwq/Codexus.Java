package codexus.csharpsup;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import java.io.IOException;
import java.lang.annotation.*;
import java.lang.reflect.InvocationTargetException;

/** Handles System.Text.Json's string-enum format on raw integer transport fields. */
public final class DotNetStringEnum {
    private DotNetStringEnum() {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Type {
        Class<? extends Enum<?>> value();
    }

    public static final class Serializer extends JsonSerializer<Integer> implements ContextualSerializer {
        private final Class<? extends Enum<?>> type;

        public Serializer() {
            this(null);
        }

        private Serializer(Class<? extends Enum<?>> type) {
            this.type = type;
        }

        @Override
        public JsonSerializer<?> createContextual(SerializerProvider provider, BeanProperty property) {
            return new Serializer(property.getAnnotation(Type.class).value());
        }

        @Override
        public void serialize(Integer raw, JsonGenerator generator, SerializerProvider provider) throws IOException {
            try {
                Enum<?> known = (Enum<?>) type.getMethod("fromValue", long.class).invoke(null, raw.longValue());
                if (known == null) {
                    generator.writeNumber(raw);
                } else {
                    generator.writeString(known.name());
                }
            } catch (ReflectiveOperationException exception) {
                throw JsonMappingException.from(generator, "Cannot resolve enum value", exception);
            }
        }
    }

    public static final class Deserializer extends JsonDeserializer<Integer> implements ContextualDeserializer {
        private final Class<? extends Enum<?>> type;

        public Deserializer() {
            this(null);
        }

        private Deserializer(Class<? extends Enum<?>> type) {
            this.type = type;
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext context, BeanProperty property) {
            return new Deserializer(property.getAnnotation(Type.class).value());
        }

        @Override
        public Integer deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            if (parser.currentToken() == JsonToken.VALUE_NUMBER_INT) {
                return parser.getIntValue();
            }
            if (parser.currentToken() != JsonToken.VALUE_STRING) {
                throw JsonMappingException.from(parser, "Expected enum name or integer");
            }
            String text = parser.getText().strip();
            try {
                if (text.matches("[+-]?[0-9]+")) {
                    return Integer.parseInt(text);
                }
                int result = 0;
                for (String name : text.split(",", -1)) {
                    Enum<?> match = null;
                    for (Enum<?> candidate : type.getEnumConstants()) {
                        if (candidate.name().equalsIgnoreCase(name.strip())) {
                            match = candidate;
                            break;
                        }
                    }
                    if (match == null) {
                        throw new IllegalArgumentException("Unknown enum name: " + name);
                    }
                    result |= ((Number) type.getMethod("value").invoke(match)).intValue();
                }
                return result;
            } catch (ReflectiveOperationException | IllegalArgumentException exception) {
                throw JsonMappingException.from(parser, "Invalid enum value", exception);
            }
        }

        @Override
        public Integer getNullValue(DeserializationContext context) throws JsonMappingException {
            throw JsonMappingException.from(context, "Cannot convert null to enum");
        }
    }
}
