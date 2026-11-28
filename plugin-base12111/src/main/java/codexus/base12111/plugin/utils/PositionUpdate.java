package codexus.base12111.plugin.utils;

import io.netty.buffer.ByteBuf;

/** Position, velocity and rotation shared by absolute sync and relative teleports. */
public record PositionUpdate(double x, double y, double z, double dx, double dy, double dz, float yaw, float pitch) {
    private static final float[] SIN = new float[65536];
    static { for (int i = 0; i < SIN.length; i++) SIN[i] = (float) Math.sin(i * Math.PI * 2.0 / 65536.0); }
    public static PositionUpdate read(ByteBuf buffer) {
        return new PositionUpdate(buffer.readDouble(), buffer.readDouble(), buffer.readDouble(),
                buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readFloat(), buffer.readFloat());
    }
    public void write(ByteBuf buffer) {
        buffer.writeDouble(x).writeDouble(y).writeDouble(z).writeDouble(dx).writeDouble(dy).writeDouble(dz).writeFloat(yaw).writeFloat(pitch);
    }
    public void apply(Entity entity, int relative) {
        float nextYaw = yaw + ((relative & 8) != 0 ? entity.Yaw : 0);
        float nextPitch = Math.clamp(pitch + ((relative & 16) != 0 ? entity.Pitch : 0), -90, 90);
        double vx = entity.VelocityX, vy = entity.VelocityY, vz = entity.VelocityZ;
        if ((relative & 256) != 0) {
            float rx = (float) Math.toRadians(entity.Pitch - nextPitch);
            float ry = (float) Math.toRadians(entity.Yaw - nextYaw);
            double oldY = vy;
            vy = oldY * cos(rx) + vz * sin(rx); vz = vz * cos(rx) - oldY * sin(rx);
            double oldX = vx;
            vx = oldX * cos(ry) + vz * sin(ry); vz = vz * cos(ry) - oldX * sin(ry);
        }
        entity.X = x + ((relative & 1) != 0 ? entity.X : 0);
        entity.Y = y + ((relative & 2) != 0 ? entity.Y : 0);
        entity.Z = z + ((relative & 4) != 0 ? entity.Z : 0);
        entity.Yaw = nextYaw; entity.Pitch = nextPitch;
        entity.VelocityX = dx + ((relative & 32) != 0 ? vx : 0);
        entity.VelocityY = dy + ((relative & 64) != 0 ? vy : 0);
        entity.VelocityZ = dz + ((relative & 128) != 0 ? vz : 0);
    }
    private static float sin(float angle) { return SIN[(int) (angle * 10430.378F) & 65535]; }
    private static float cos(float angle) { return SIN[(int) (angle * 10430.378F + 16384.0F) & 65535]; }
}
