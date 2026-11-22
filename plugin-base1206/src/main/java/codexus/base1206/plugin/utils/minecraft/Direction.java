package codexus.base1206.plugin.utils.minecraft;

public enum Direction {
    Down,
    Up,
    North,
    South,
    West,
    East;
    public int value() { return ordinal(); }
    public static Direction fromValue(int id) {
        if (id < 0 || id >= values().length) throw new IllegalArgumentException("Unknown Direction: " + id);
        return values()[id];
    }
}
