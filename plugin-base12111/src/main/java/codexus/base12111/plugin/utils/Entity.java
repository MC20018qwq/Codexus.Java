package codexus.base12111.plugin.utils;

public class Entity {
    public int EntityId;
    public byte[] EntityGuid = new byte[0];
    public double X, Y, Z;
    public float Yaw, Pitch;
    public boolean OnGround, HorizontalCollision;
    public double VelocityX, VelocityY, VelocityZ;
    public void applyRelativePosition(short dx, short dy, short dz) {
        X = applyDelta(X, dx); Y = applyDelta(Y, dy); Z = applyDelta(Z, dz);
    }
    private static double applyDelta(double base, short delta) {
        return delta == 0 ? base : (Math.round(base * 4096.0) + delta) / 4096.0;
    }
}
