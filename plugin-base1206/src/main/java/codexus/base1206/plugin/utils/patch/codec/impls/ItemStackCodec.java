package codexus.base1206.plugin.utils.patch.codec.impls;

import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.OptionalInt;
import codexus.base1206.plugin.utils.minecraft.*;
import codexus.base1206.plugin.utils.patch.codec.IStreamCodec;
import static codexus.base1206.plugin.extensions.ByteBufferExtensions.*;
import static codexus.development.sdk.extensions.NettyExtensions.*;
public final class ItemStackCodec implements IStreamCodec<ByteBuf,ItemStack> {

    public ItemStack decode(ByteBuf buffer) { int count=buffer.readUnsignedByte(); if(count==0) return ItemStack.empty(); ItemStack value=new ItemStack(); value.Count=count; value.ItemId=readVarIntFromBuffer(buffer); value.RawData=readByteArrayFromBuffer(buffer,itemStackLength(buffer)); return value; }
    public void encode(ByteBuf buffer, ItemStack value) { if(value.isEmpty()){buffer.writeByte(0);return;} buffer.writeByte(value.Count); writeVarInt(buffer,value.ItemId); if(value.RawData==null){writeVarInt(buffer,0);writeVarInt(buffer,0);}else buffer.writeBytes(value.RawData); }
}
