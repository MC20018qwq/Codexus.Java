package codexus.base12111.plugin.utils.minecraft;

public enum Pose {
    Standing,
    FallFlying,
    Sleeping,
    Swimming,
    SpinAttack,
    Crouching,
    LongJumping,
    Dying,
    Croaking,
    UsingTongue,
    Sitting,
    Roaring,
    Sniffing,
    Emerging,
    Digging,
    Sliding,
    Shooting,
    Inhaling;
    public int value() { return ordinal(); }
    public static Pose fromValue(int id) {
        if (id < 0 || id >= values().length) throw new IllegalArgumentException("Unknown Pose: " + id);
        return values()[id];
    }
}
