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
public final class PaintingVariantCodec implements IStreamCodec<ByteBuf,Holder<PaintingVariant>> {

    public Holder<PaintingVariant> decode(ByteBuf buffer) { int id=readVarIntFromBuffer(buffer); if(id==0) throw new UnsupportedOperationException("Inline PaintingVariant metadata is preserved as raw packet data"); return new Holder<>(new PaintingVariant(id)); }
    public void encode(ByteBuf buffer, Holder<PaintingVariant> value) { writeVarInt(buffer,value.Value.VariantId); }
}
