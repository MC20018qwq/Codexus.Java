package codexus.base1206.plugin.utils.minecraft;

public enum Pose {
    Standing,
    FallFlying,
    Sleeping,
    Swimming,
    SpinAttack,
    Sneaking,
    LongJumping,
    Dying,
    Croaking,
    UsingTongue,
    Sitting,
    Roaring,
    Sniffing,
    Emerging,
    Digging;
    public int value() { return ordinal(); }
    public static Pose fromValue(int id) {
        if (id < 0 || id >= values().length) throw new IllegalArgumentException("Unknown Pose: " + id);
        return values()[id];
    }
}
