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
public final class OptionalBlockStateCodec implements IStreamCodec<ByteBuf,Optional<BlockState>> {

    public Optional<BlockState> decode(ByteBuf buffer) { int id=readVarIntFromBuffer(buffer); return id==0?Optional.empty():Optional.of(new BlockState(id)); }
    public void encode(ByteBuf buffer, Optional<BlockState> value) { writeVarInt(buffer,value.map(v->v.StateId).orElse(0)); }
}
