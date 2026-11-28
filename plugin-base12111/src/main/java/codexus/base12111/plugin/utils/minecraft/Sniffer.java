package codexus.base12111.plugin.utils.minecraft;

public final class Sniffer {
    private Sniffer() {}
    public enum State { Idling, FeelingHappy, Scenting, Sniffing, Searching, Digging, Rising;
        public int value() { return ordinal(); }
        public static State fromValue(int id) {
            if (id < 0 || id >= values().length) throw new IllegalArgumentException("Unknown State: " + id);
            return values()[id];
        }
    }
}
