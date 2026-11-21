package codexus.game.launcher.services.java.rpc.events;

import codexus.csharpsup.DotNetEncoding;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.*;
import java.util.*;

public final class SimplePack {
    private SimplePack() { }
    public static byte[] pack(Object... data) {
        if (data == null) return null;
        var result = new ByteArrayOutputStream();
        for (Object value : data) {
            Objects.requireNonNull(value);
            if (value instanceof byte[] bytes) result.writeBytes(bytes);
            else if (value instanceof Boolean flag) result.write(flag ? 1 : 0);
            else if (value instanceof Byte number) result.write(number);
            else if (value instanceof Short number) result.writeBytes(little(2).putShort(number).array());
            else if (value instanceof Integer number) result.writeBytes(little(4).putInt(number).array());
            else if (value instanceof Long number) result.writeBytes(little(8).putLong(number).array());
            else if (value instanceof Double number) result.writeBytes(little(8).putDouble(number).array());
            else if (value instanceof String text) {
                byte[] bytes = DotNetEncoding.utf8(text);
                result.writeBytes(little(2).putShort((short) bytes.length).array()); result.writeBytes(bytes);
            } else if (value instanceof Enum<?> enumeration) {
                try {
                    Number number = (Number) enumeration.getDeclaringClass().getMethod("value").invoke(enumeration);
                    result.writeBytes(little(4).putInt(number.intValue()).array());
                } catch (ReflectiveOperationException e) { throw new IllegalArgumentException("Enum has no numeric protocol value", e); }
            } else if (value instanceof List<?> values) {
                if (values.isEmpty()) { result.write(0); result.write(0); }
                else if (values.stream().allMatch(Integer.class::isInstance)) {
                    result.writeBytes(little(2).putShort((short) (values.size() * 4)).array());
                    for (Object item : values) result.writeBytes(little(4).putInt((Integer) item).array());
                } else if (values.stream().allMatch(Long.class::isInstance) || values.stream().allMatch(BigInteger.class::isInstance)) {
                    result.writeBytes(little(2).putShort((short) (values.size() * 8)).array());
                    for (Object item : values) result.writeBytes(little(8).putLong(((Number) item).longValue()).array());
                }
            }
            // Scalar UInt64 (BigInteger), Single and other object types are ignored by the original.
        }
        return result.toByteArray();
    }
    private static ByteBuffer little(int count) { return ByteBuffer.allocate(count).order(ByteOrder.LITTLE_ENDIAN); }
}
