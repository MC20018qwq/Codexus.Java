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
public final class CompoundTagCodec implements IStreamCodec<ByteBuf,CompoundTag> {

    public CompoundTag decode(ByteBuf buffer) { return new CompoundTag(readByteArrayFromBuffer(buffer,nbtLength(buffer))); }
    public void encode(ByteBuf buffer, CompoundTag value) { if(value.RawData==null) throw new IllegalStateException("CompoundTag RawData is null"); buffer.writeBytes(value.RawData); }
}
