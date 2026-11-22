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
public final class ComponentCodec implements IStreamCodec<ByteBuf,Component> {
    private final CompoundTagCodec nbt = new CompoundTagCodec();
    public Component decode(ByteBuf buffer) { return new Component(nbt.decode(buffer)); }
    public void encode(ByteBuf buffer, Component value) { nbt.encode(buffer,value.Tag); }
}
