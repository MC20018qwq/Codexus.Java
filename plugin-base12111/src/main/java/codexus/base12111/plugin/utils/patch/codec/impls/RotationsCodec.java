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
public final class RotationsCodec implements IStreamCodec<ByteBuf,Rotations> {

    public Rotations decode(ByteBuf buffer) { return new Rotations(buffer.readFloat(),buffer.readFloat(),buffer.readFloat()); }
    public void encode(ByteBuf buffer, Rotations value) { buffer.writeFloat(value.X); buffer.writeFloat(value.Y); buffer.writeFloat(value.Z); }
}
