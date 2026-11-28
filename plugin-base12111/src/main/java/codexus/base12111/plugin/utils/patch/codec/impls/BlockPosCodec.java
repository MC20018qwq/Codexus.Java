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
public final class BlockPosCodec implements IStreamCodec<ByteBuf,BlockPos> {

    public BlockPos decode(ByteBuf buffer) { long packed=buffer.readLong(); return new BlockPos((int)(packed>>38),(int)(packed<<52>>52),(int)(packed<<26>>38)); }
    public void encode(ByteBuf buffer, BlockPos value) { buffer.writeLong(((value.X & 0x3ffffffL)<<38) | (value.Y & 0xfffL) | ((value.Z & 0x3ffffffL)<<12)); }
}
