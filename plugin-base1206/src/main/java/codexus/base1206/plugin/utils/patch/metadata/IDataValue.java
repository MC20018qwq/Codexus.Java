package codexus.base1206.plugin.utils.patch.metadata;

import io.netty.buffer.ByteBuf;
public interface IDataValue {
    int id();
    int serializerId();
    void write(ByteBuf buffer);
}
