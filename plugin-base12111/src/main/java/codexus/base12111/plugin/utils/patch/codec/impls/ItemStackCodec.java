package codexus.base12111.plugin.utils.patch.codec.impls;

import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.OptionalInt;
import codexus.base12111.plugin.utils.minecraft.*;
import codexus.base12111.plugin.utils.patch.codec.IStreamCodec;
import static codexus.base12111.plugin.extensions.ByteBufferExtensions.*;
import static codexus.development.sdk.extensions.NettyExtensions.*;
public final class ItemStackCodec implements IStreamCodec<ByteBuf,ItemStack> {

    public ItemStack decode(ByteBuf buffer) { int count=readVarIntFromBuffer(buffer); if(count<0) throw new IllegalArgumentException("Negative item count"); if(count==0) return ItemStack.empty(); ItemStack value=new ItemStack(); value.Count=count; value.ItemId=readVarIntFromBuffer(buffer); value.RawData=readByteArrayFromBuffer(buffer,itemStackLength(buffer)); return value; }
    public void encode(ByteBuf buffer, ItemStack value) { if(value.isEmpty()){writeVarInt(buffer,0);return;} writeVarInt(buffer,value.Count); writeVarInt(buffer,value.ItemId); if(value.RawData==null){writeVarInt(buffer,0);writeVarInt(buffer,0);}else buffer.writeBytes(value.RawData); }
}
