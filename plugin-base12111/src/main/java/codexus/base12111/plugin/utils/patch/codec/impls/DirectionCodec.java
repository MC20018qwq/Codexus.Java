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
public final class DirectionCodec implements IStreamCodec<ByteBuf,Direction> {

    public Direction decode(ByteBuf buffer) { return Direction.fromValue(readVarIntFromBuffer(buffer)); }
    public void encode(ByteBuf buffer, Direction value) { writeVarInt(buffer,value.value()); }
}
