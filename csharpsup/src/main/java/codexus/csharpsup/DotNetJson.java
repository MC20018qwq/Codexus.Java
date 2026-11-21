package codexus.csharpsup;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.*;
import com.fasterxml.jackson.databind.type.LogicalType;
import java.io.*;
import java.lang.reflect.*;
import java.math.BigInteger;
import java.util.*;

public final class DotNetJson {
    private static final ObjectMapper MAPPER = createMapper();
    private static final ObjectMapper RELAXED = relaxedMapper();
    private static ObjectMapper relaxedMapper() {
        ObjectMapper mapper = MAPPER.copy(); mapper.getFactory().setCharacterEscapes(new DotNetJsonEscapes(true)); return mapper;
    }
    private DotNetJson() {}
    public static String serializeRelaxed(Object value) {
        try { return RELAXED.writeValueAsString(value); } catch (IOException e) { throw new IllegalArgumentException("JSON serialization failed", e); }
    }
    public static String serializeRelaxedIndented(Object value) {
        try { return RELAXED.writerWithDefaultPrettyPrinter().writeValueAsString(value); }
        catch (IOException e) { throw new IllegalArgumentException("JSON serialization failed", e); }
    }
    public static <T> T deserializeWithStringEnums(String json, TypeReference<T> type) {
        JavaType target = MAPPER.getTypeFactory().constructType(type);
        JsonNode node = normalizeStringEnums(parse(json), target);
        return read(node.toString(), target);
    }
    private static JsonNode normalizeStringEnums(JsonNode node, JavaType type) {
        if (node == null || node.isNull()) return node;
        if (type.isCollectionLikeType() || type.isArrayType()) {
            if (node.isArray()) for (int i = 0; i < node.size(); i++) ((com.fasterxml.jackson.databind.node.ArrayNode) node).set(i, normalizeStringEnums(node.get(i), type.getContentType()));
            return node;
        }
        if (!node.isObject() || type.getRawClass().getName().startsWith("java.")) return node;
        var object = (com.fasterxml.jackson.databind.node.ObjectNode) node;
        for (Field field : type.getRawClass().getFields()) {
            if (Modifier.isStatic(field.getModifiers()) || field.isAnnotationPresent(JsonIgnore.class)) continue;
            JsonProperty property = field.getAnnotation(JsonProperty.class);
            String name = property == null ? field.getName() : property.value();
            JsonNode value = object.get(name);
            if (value == null) {
                String match = null;
                for (var keys = object.fieldNames(); keys.hasNext();) { String key = keys.next(); if (key.equalsIgnoreCase(name)) match = key; }
                if (match != null) { value = object.remove(match); object.set(name, value); }
            }
            if (value == null || value.isNull()) continue;
            if (value.isTextual() && (field.getType() == int.class || field.getType() == long.class)) {
                try {
                    Class<?> enumeration = type.getRawClass().getMethod("get" + field.getName()).getReturnType();
                    if (enumeration.isEnum()) {
                        String text = value.asText().strip(); long raw;
                        if (text.matches("[+-]?[0-9]+")) raw = Long.parseLong(text);
                        else {
                            raw = 0;
                            for (String part : text.split(",", -1)) {
                                Object found = null;
                                for (Object item : enumeration.getEnumConstants()) if (((Enum<?>) item).name().equalsIgnoreCase(part.strip())) found = item;
                                if (found == null) throw new IllegalArgumentException("Unknown enum name: " + part);
                                raw |= ((Number) enumeration.getMethod("value").invoke(found)).longValue();
                            }
                        }
                        value = com.fasterxml.jackson.databind.node.LongNode.valueOf(raw); object.set(name, value);
                    }
                } catch (NoSuchMethodException ignored) {
                } catch (ReflectiveOperationException e) { throw new IllegalArgumentException("Cannot read enum", e); }
            }
            object.set(name, normalizeStringEnums(value, MAPPER.getTypeFactory().resolveMemberType(field.getGenericType(), type.getBindings())));
        }
        return node;
    }
    private static ObjectMapper createMapper() {
        ObjectMapper m = new ObjectMapper();
        m.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        m.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.PUBLIC_ONLY);
        m.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        m.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        m.disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT);
        m.enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        for (LogicalType type : new LogicalType[]{LogicalType.Integer, LogicalType.Float, LogicalType.Boolean}) {
            m.coercionConfigFor(type).setCoercion(CoercionInputShape.String, CoercionAction.Fail).setCoercion(CoercionInputShape.EmptyString, CoercionAction.Fail);
        }
        m.coercionConfigFor(LogicalType.Textual).setCoercion(CoercionInputShape.Integer, CoercionAction.Fail).setCoercion(CoercionInputShape.Float, CoercionAction.Fail).setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail);
        com.fasterxml.jackson.databind.module.SimpleModule compatibility = new com.fasterxml.jackson.databind.module.SimpleModule();
        compatibility.addSerializer(String.class, new JsonSerializer<String>() {
            @Override public void serialize(String value, JsonGenerator generator, SerializerProvider provider) throws IOException {
                generator.writeString(DotNetEncoding.utf8(DotNetEncoding.utf8(value)));
            }
        });
        JsonSerializer<Float> floatSerializer = new JsonSerializer<Float>() {
            @Override public void serialize(Float value, JsonGenerator generator, SerializerProvider provider) throws IOException {
                if (!Float.isFinite(value)) throw new IllegalArgumentException("Non-finite JSON number");
                String text = Float.toString(value);
                if (text.endsWith(".0")) text = text.substring(0, text.length() - 2);
                generator.writeNumber(text);
            }
        };
        compatibility.addSerializer(Float.class, floatSerializer);
        compatibility.addSerializer(Float.TYPE, floatSerializer);
        m.registerModule(compatibility);
        m.getFactory().setCharacterEscapes(new DotNetJsonEscapes(false));
        return m;
    }
    public static String serialize(Object value) {
        try { return MAPPER.writeValueAsString(value); } catch (IOException e) { throw new IllegalArgumentException("JSON serialization failed", e); }
    }
    public static <T> T deserialize(String json, Class<T> type) { return read(json, MAPPER.constructType(type)); }
    public static <T> T deserialize(String json, TypeReference<T> type) { return read(json, MAPPER.getTypeFactory().constructType(type)); }
    private static <T> T read(String json, JavaType type) {
        try { JsonNode node=MAPPER.readTree(json); if(node==null)throw new IllegalArgumentException("No JSON tokens");validate(node,type,"$");return MAPPER.readerFor(type).readValue(node); }
        catch(IOException e){throw new IllegalArgumentException("JSON deserialization failed",e);}
    }
    public static JsonNode parse(String json) { try{return MAPPER.readTree(json);}catch(IOException e){throw new IllegalArgumentException("Invalid JSON",e);} }
    public static <T> T convert(Object value, Class<T> type) { return deserialize(serialize(value),type); }
    private static void validate(JsonNode node, JavaType type, String path) {
        if(node==null||node.isNull())return;
        if(type.isArrayType()||type.isCollectionLikeType()) {if(node.isArray())for(JsonNode child:node)validate(child,type.getContentType(),path+"[]");return;}
        Class<?> cls=type.getRawClass();
        if(cls.isPrimitive()||cls.isEnum()||cls.getName().startsWith("java."))return;
        if(!node.isObject())return;
        for(Field field:cls.getFields()) {
            if(Modifier.isStatic(field.getModifiers())||field.isAnnotationPresent(JsonIgnore.class))continue;
            JsonProperty prop=field.getAnnotation(JsonProperty.class);String key=prop==null?field.getName():prop.value();
            JsonNode child=node.get(key);
            if(child==null&&field.isAnnotationPresent(DotNetRequired.class))throw new IllegalArgumentException("Missing required property "+path+"."+key);
            if(child==null||child.isNull())continue;
            Unsigned unsigned=field.getAnnotation(Unsigned.class);
            if(unsigned!=null) {
                if(!child.isIntegralNumber()||child.bigIntegerValue().signum()<0||child.bigIntegerValue().bitLength()>unsigned.value())throw new IllegalArgumentException("Unsigned integer out of range: "+path+"."+key);
            }
            validate(child,MAPPER.getTypeFactory().resolveMemberType(field.getGenericType(),type.getBindings()),path+"."+key);
        }
    }
}
