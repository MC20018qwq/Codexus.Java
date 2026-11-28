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
public final class PaintingVariantCodec implements IStreamCodec<ByteBuf,Holder<PaintingVariant>> {

    public Holder<PaintingVariant> decode(ByteBuf buffer) {
        int start = buffer.readerIndex();
        int id = readVarIntFromBuffer(buffer);
        buffer.readerIndex(start);
        skipPaintingVariant(buffer);
        PaintingVariant value = new PaintingVariant(id);
        value.RawData = new byte[buffer.readerIndex() - start];
        buffer.getBytes(start, value.RawData);
        return new Holder<>(value);
    }
    public void encode(ByteBuf buffer, Holder<PaintingVariant> value) {
        if (value.Value.RawData != null) buffer.writeBytes(value.Value.RawData);
        else writeVarInt(buffer, value.Value.VariantId);
    }
}
