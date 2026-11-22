package codexus.base1206.plugin.utils.minecraft;

public final class Armadillo {
    private Armadillo() {}
    public enum ArmadilloState { Idle, Rolling, Scared, Unrolling;
        public int value() { return ordinal(); }
        public static ArmadilloState fromValue(int id) {
            if (id < 0 || id >= values().length) throw new IllegalArgumentException("Unknown ArmadilloState: " + id);
            return values()[id];
        }
    }
}
