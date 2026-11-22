package codexus.base1206.plugin.utils.patch.metadata;

import io.netty.buffer.ByteBuf;
import codexus.base1206.plugin.utils.patch.codec.IEntityDataSerializer;
import static codexus.development.sdk.extensions.NettyExtensions.*;
public final class DataValue<T> implements IDataValue {
    public final int Id;
    public final IEntityDataSerializer<T> Serializer;
    public final T Value;
    public DataValue(int id, IEntityDataSerializer<T> serializer, T value) { Id=id; Serializer=serializer; Value=value; }
    public int id() { return Id; }
    public int serializerId() { return EntityDataSerializers.getSerializedId(Serializer); }
    public static IDataValue read(ByteBuf buffer, int id) { return EntityDataSerializers.getSerializer(readVarIntFromBuffer(buffer)).read(buffer,id); }
    public void write(ByteBuf buffer) { buffer.writeByte(Id); writeVarInt(buffer,serializerId()); Serializer.codec().encode(buffer,Value); }
}
