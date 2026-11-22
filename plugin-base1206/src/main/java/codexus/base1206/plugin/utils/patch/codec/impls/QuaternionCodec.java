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
public final class QuaternionCodec implements IStreamCodec<ByteBuf,Quaternionf> {

    public Quaternionf decode(ByteBuf buffer) { return new Quaternionf(buffer.readFloat(),buffer.readFloat(),buffer.readFloat(),buffer.readFloat()); }
    public void encode(ByteBuf buffer, Quaternionf value) { buffer.writeFloat(value.X); buffer.writeFloat(value.Y); buffer.writeFloat(value.Z); buffer.writeFloat(value.W); }
}
