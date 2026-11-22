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
public final class ParticleCodec implements IStreamCodec<ByteBuf,ParticleOptions> {

    public ParticleOptions decode(ByteBuf buffer) { throw new UnsupportedOperationException("Particle metadata is preserved as raw packet data"); }
    public void encode(ByteBuf buffer, ParticleOptions value) { throw new UnsupportedOperationException("Particle metadata is preserved as raw packet data"); }
}
