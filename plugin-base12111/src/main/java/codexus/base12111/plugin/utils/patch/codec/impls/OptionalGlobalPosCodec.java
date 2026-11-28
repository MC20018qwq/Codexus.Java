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
public final class OptionalGlobalPosCodec implements IStreamCodec<ByteBuf,Optional<GlobalPos>> {
    private final BlockPosCodec pos = new BlockPosCodec();
    public Optional<GlobalPos> decode(ByteBuf buffer) { if(!buffer.readBoolean()) return Optional.empty(); String dimension=readStringFromBuffer(buffer,32767); return Optional.of(new GlobalPos(dimension,pos.decode(buffer))); }
    public void encode(ByteBuf buffer, Optional<GlobalPos> value) { buffer.writeBoolean(value.isPresent()); value.ifPresent(v->{writeStringToBuffer(buffer,v.Dimension,32767);pos.encode(buffer,v.Position);}); }
}
