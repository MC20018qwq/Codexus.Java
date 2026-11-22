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
public final class OptionalUuidCodec implements IStreamCodec<ByteBuf,Optional<byte[]>> {

    public Optional<byte[]> decode(ByteBuf buffer) { return buffer.readBoolean()?Optional.of(readByteArrayFromBuffer(buffer,16)):Optional.empty(); }
    public void encode(ByteBuf buffer, Optional<byte[]> value) { buffer.writeBoolean(value.isPresent()); value.ifPresent(v->{if(v.length!=16) throw new IllegalArgumentException("UUID must contain 16 bytes"); buffer.writeBytes(v);}); }
}
