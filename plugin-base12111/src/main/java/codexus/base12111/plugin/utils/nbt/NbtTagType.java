package codexus.base12111.plugin.utils.nbt;

public enum NbtTagType {
    TagEnd, TagByte, TagShort, TagInt, TagLong, TagFloat, TagDouble, TagByteArray, TagString, TagList, TagCompound, TagIntArray, TagLongArray;
    public int value() {return ordinal();}
    public static NbtTagType fromValue(int id) {if(id<0 || id>=values().length)throw new IllegalArgumentException("Unknown NBT tag type: "+id); return values()[id];}
}
