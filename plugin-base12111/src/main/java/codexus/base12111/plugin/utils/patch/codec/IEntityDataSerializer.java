package codexus.base12111.plugin.utils.patch.codec;

import io.netty.buffer.ByteBuf;
import codexus.base12111.plugin.utils.patch.metadata.DataValue;
public interface IEntityDataSerializer<T> {
    IStreamCodec<ByteBuf,T> codec();
    default DataValue<T> read(ByteBuf buffer, int id) { return new DataValue<>(id, this, codec().decode(buffer)); }
    static <T> IEntityDataSerializer<T> forValueType(IStreamCodec<ByteBuf,T> codec) { return new EntityDataSerializerRef<>(codec); }
}
