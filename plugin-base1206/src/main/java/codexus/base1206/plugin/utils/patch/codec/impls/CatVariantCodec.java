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
public final class CatVariantCodec implements IStreamCodec<ByteBuf,Holder<CatVariant>> {

    public Holder<CatVariant> decode(ByteBuf buffer) { int id=readVarIntFromBuffer(buffer); return new Holder<>(new CatVariant(id)); }
    public void encode(ByteBuf buffer, Holder<CatVariant> value) { writeVarInt(buffer,value.Value.VariantId); }
}
