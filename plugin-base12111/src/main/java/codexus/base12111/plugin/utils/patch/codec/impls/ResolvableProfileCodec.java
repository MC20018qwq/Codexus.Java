package codexus.base12111.plugin.utils.patch.codec.impls;

import io.netty.buffer.ByteBuf;
import codexus.base12111.plugin.utils.patch.codec.IStreamCodec;
import static codexus.development.sdk.extensions.NettyExtensions.*;

/** Keeps the exact profile/property/skin bytes while locating the next metadata value. */
public final class ResolvableProfileCodec implements IStreamCodec<ByteBuf, byte[]> {
    @Override public byte[] decode(ByteBuf buffer) {
        int start = buffer.readerIndex();
        if (buffer.readBoolean()) { buffer.skipBytes(16); readStringFromBuffer(buffer, 16); }
        else {
            if (buffer.readBoolean()) readStringFromBuffer(buffer, 16);
            if (buffer.readBoolean()) buffer.skipBytes(16);
        }
        int count = readVarIntFromBuffer(buffer);
        if (count < 0 || count > buffer.readableBytes()) throw new IllegalArgumentException("Invalid profile property count");
        for (int i = 0; i < count; i++) {
            readStringFromBuffer(buffer, 32767); readStringFromBuffer(buffer, 32767);
            if (buffer.readBoolean()) readStringFromBuffer(buffer, 32767);
        }
        for (int i = 0; i < 3; i++) if (buffer.readBoolean()) readStringFromBuffer(buffer, 32767);
        if (buffer.readBoolean()) buffer.readBoolean();
        byte[] raw = new byte[buffer.readerIndex() - start]; buffer.getBytes(start, raw); return raw;
    }
    @Override public void encode(ByteBuf buffer, byte[] value) { buffer.writeBytes(value); }
}
