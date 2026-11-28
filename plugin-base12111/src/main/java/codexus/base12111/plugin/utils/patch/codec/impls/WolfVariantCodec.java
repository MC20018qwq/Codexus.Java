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
public final class WolfVariantCodec implements IStreamCodec<ByteBuf,Holder<WolfVariant>> {

    public Holder<WolfVariant> decode(ByteBuf buffer) { int id=readVarIntFromBuffer(buffer); return new Holder<>(new WolfVariant(id)); }
    public void encode(ByteBuf buffer, Holder<WolfVariant> value) { writeVarInt(buffer,value.Value.VariantId); }
}
