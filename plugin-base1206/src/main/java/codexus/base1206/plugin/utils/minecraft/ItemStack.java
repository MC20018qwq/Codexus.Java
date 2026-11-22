package codexus.base1206.plugin.utils.minecraft;

public class ItemStack {
    public int Count, ItemId;
    public byte[] RawData;
    public static ItemStack empty() { return new ItemStack(); }
    public boolean isEmpty() { return Count <= 0; }
}
