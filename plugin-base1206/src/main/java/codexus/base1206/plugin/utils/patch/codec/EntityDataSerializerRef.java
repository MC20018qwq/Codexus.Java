package codexus.base1206.plugin.utils.patch.codec;

import io.netty.buffer.ByteBuf;
public record EntityDataSerializerRef<T>(IStreamCodec<ByteBuf,T> codec) implements IEntityDataSerializer<T> {}
