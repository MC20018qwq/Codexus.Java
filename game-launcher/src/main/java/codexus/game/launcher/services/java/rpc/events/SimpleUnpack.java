package codexus.game.launcher.services.java.rpc.events;

import codexus.csharpsup.*;
import java.lang.reflect.*;
import java.nio.*;
import java.util.*;

public class SimpleUnpack {
    private final ByteBuffer data;
    private int lastLength;
    public SimpleUnpack(byte[] bytes) { data = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN); }
    public <T> T unpack(T content) {
        if (content == null) return null;
        try {
            for (Field field : content.getClass().getFields()) {
                Object value = field.get(content);
                if (value != null) field.set(content, innerUnpack(value, field));
            }
            return content;
        } catch (IllegalAccessException e) { throw new IllegalArgumentException("Cannot unpack RPC fields", e); }
    }
    private Object innerUnpack(Object value, Field field) {
        Class<?> type = field.getType();
        Unsigned unsigned = field.getAnnotation(Unsigned.class);
        int width = unsigned == null ? 0 : unsigned.value();
        if (type == byte.class || type == Byte.class) return data.get();
        if (width == 8) return (int) (data.get() & 255);
        if (width == 16) { lastLength = data.getShort() & 65535; return lastLength; }
        if (width == 32 && (type == long.class || type == Long.class)) return Integer.toUnsignedLong(data.getInt());
        if (type == short.class || type == Short.class) return data.getShort();
        if (type == int.class || type == Integer.class) return data.getInt();
        if (type == String.class) {
            byte[] bytes = new byte[lastLength]; data.get(bytes); return DotNetEncoding.utf8(bytes);
        }
        if (type == byte[].class) { byte[] bytes = new byte[lastLength]; data.get(bytes); return bytes; }
        if (value instanceof List<?> && field.getGenericType() instanceof ParameterizedType generic) {
            var item = generic.getActualTypeArguments()[0];
            if (item == Integer.class || (item == Long.class && width == 32)) {
                var result = new ArrayList<Object>();
                for (int remaining = lastLength; remaining > 0; remaining = (remaining - 4) & 65535) {
                    int number = data.getInt();
                    if (item == Long.class) result.add(Integer.toUnsignedLong(number)); else result.add(number);
                }
                return result;
            }
        }
        return value;
    }
}
