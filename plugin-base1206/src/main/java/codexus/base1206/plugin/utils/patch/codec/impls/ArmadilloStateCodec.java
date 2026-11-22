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
public final class ArmadilloStateCodec implements IStreamCodec<ByteBuf,Armadillo.ArmadilloState> {

    public Armadillo.ArmadilloState decode(ByteBuf buffer) { return Armadillo.ArmadilloState.fromValue(readVarIntFromBuffer(buffer)); }
    public void encode(ByteBuf buffer, Armadillo.ArmadilloState value) { writeVarInt(buffer,value.value()); }
}
