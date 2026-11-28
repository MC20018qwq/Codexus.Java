package codexus.base12111.plugin.utils;

import io.netty.buffer.ByteBuf;
import static codexus.development.sdk.extensions.NettyExtensions.*;

/** The low-precision velocity vector used by entity spawning. */
public record PackedVelocity(double x, double y, double z) {
    public static PackedVelocity read(ByteBuf buffer) {
        int first = buffer.readUnsignedByte();
        if (first == 0) return new PackedVelocity(0, 0, 0);
        int second = buffer.readUnsignedByte();
        long bits = (buffer.readUnsignedInt() << 16) | ((long) second << 8) | first;
        long scale = first & 3;
        if ((first & 4) != 0) scale |= Integer.toUnsignedLong(readVarIntFromBuffer(buffer)) << 2;
        return new PackedVelocity(unpack(bits >>> 3) * scale, unpack(bits >>> 18) * scale, unpack(bits >>> 33) * scale);
    }
    public void write(ByteBuf buffer) {
        double a = sanitize(x), b = sanitize(y), c = sanitize(z);
        double max = Math.max(Math.abs(a), Math.max(Math.abs(b), Math.abs(c)));
        if (max < 3.051944088384301E-5) { buffer.writeByte(0); return; }
        long scale = (long) Math.ceil(max);
        boolean extended = (scale & 3) != scale;
        long bits = (extended ? (scale & 3) | 4 : scale)
                | pack(a / scale) << 3 | pack(b / scale) << 18 | pack(c / scale) << 33;
        buffer.writeByte((int) bits).writeByte((int) (bits >>> 8)).writeInt((int) (bits >>> 16));
        if (extended) writeVarInt(buffer, (int) (scale >>> 2));
    }
    private static double sanitize(double value) { return Double.isNaN(value) ? 0 : Math.clamp(value, -1.7179869183E10, 1.7179869183E10); }
    private static long pack(double value) { return Math.round((value * 0.5 + 0.5) * 32766.0); }
    private static double unpack(long value) { return Math.min(value & 32767L, 32766.0) * 2.0 / 32766.0 - 1.0; }
}
