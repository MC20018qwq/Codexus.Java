package codexus.base12111.plugin.utils.patch.codec.impls;

import io.netty.buffer.ByteBuf;
import codexus.base12111.plugin.utils.minecraft.ParticleOptions;
import codexus.base12111.plugin.utils.patch.codec.IStreamCodec;
import static codexus.development.sdk.extensions.NettyExtensions.*;

public final class ParticleCodec implements IStreamCodec<ByteBuf, ParticleOptions> {
    @Override public ParticleOptions decode(ByteBuf buffer) {
        int id = readVarIntFromBuffer(buffer), start = buffer.readerIndex();
        if (id < 0 || id >= 115) throw new UnsupportedOperationException("Unknown particle id: " + id);
        switch (id) {
            case 1, 2, 29, 103, 109, 113 -> readVarIntFromBuffer(buffer);
            case 8, 21, 36, 38, 42 -> buffer.skipBytes(4);
            case 14, 16, 46 -> buffer.skipBytes(8);
            case 15 -> buffer.skipBytes(12);
            case 47 -> new ItemStackCodec().decode(buffer);
            case 48 -> {
                int source = readVarIntFromBuffer(buffer);
                if (source == 0) buffer.skipBytes(8);
                else if (source == 1) { readVarIntFromBuffer(buffer); buffer.skipBytes(4); }
                else throw new UnsupportedOperationException("Unknown vibration source: " + source);
                readVarIntFromBuffer(buffer);
            }
            case 49 -> { buffer.skipBytes(28); readVarIntFromBuffer(buffer); }
            default -> { }
        }
        byte[] raw = new byte[buffer.readerIndex() - start]; buffer.getBytes(start, raw);
        return new ParticleOptions(id, raw);
    }
    @Override public void encode(ByteBuf buffer, ParticleOptions value) {
        writeVarInt(buffer, value.ParticleId); buffer.writeBytes(value.RawData);
    }
}
