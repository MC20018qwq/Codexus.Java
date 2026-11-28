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
public final class ParticleListCodec implements IStreamCodec<ByteBuf,List<ParticleOptions>> {
    private final ParticleCodec inner=new ParticleCodec();
    public List<ParticleOptions> decode(ByteBuf buffer) { int count=readVarIntFromBuffer(buffer); if(count<0) throw new IllegalArgumentException("Negative particle count"); var list=new ArrayList<ParticleOptions>(); for(int i=0;i<count;i++)list.add(inner.decode(buffer)); return list; }
    public void encode(ByteBuf buffer, List<ParticleOptions> value) { writeVarInt(buffer,value.size()); for(var particle:value) inner.encode(buffer,particle); }
}
