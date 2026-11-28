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
public final class OptionalComponentCodec implements IStreamCodec<ByteBuf,Optional<Component>> {
    private final ComponentCodec inner = new ComponentCodec();
    public Optional<Component> decode(ByteBuf buffer) { return buffer.readBoolean()?Optional.of(inner.decode(buffer)):Optional.empty(); }
    public void encode(ByteBuf buffer, Optional<Component> value) { buffer.writeBoolean(value.isPresent()); value.ifPresent(v->inner.encode(buffer,v)); }
}
