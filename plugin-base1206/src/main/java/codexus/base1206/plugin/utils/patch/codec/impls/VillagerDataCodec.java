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
public final class VillagerDataCodec implements IStreamCodec<ByteBuf,VillagerData> {

    public VillagerData decode(ByteBuf buffer) { return new VillagerData(readVarIntFromBuffer(buffer),readVarIntFromBuffer(buffer),readVarIntFromBuffer(buffer)); }
    public void encode(ByteBuf buffer, VillagerData value) { writeVarInt(buffer,value.VillagerType); writeVarInt(buffer,value.VillagerProfession); writeVarInt(buffer,value.Level); }
}
