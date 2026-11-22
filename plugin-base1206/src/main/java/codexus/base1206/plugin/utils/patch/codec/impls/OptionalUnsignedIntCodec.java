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
public final class OptionalUnsignedIntCodec implements IStreamCodec<ByteBuf,OptionalInt> {

    public OptionalInt decode(ByteBuf buffer) { int value=readVarIntFromBuffer(buffer); return value==0?OptionalInt.empty():OptionalInt.of(value-1); }
    public void encode(ByteBuf buffer, OptionalInt value) { writeVarInt(buffer,value.isPresent()?value.getAsInt()+1:0); }
}
