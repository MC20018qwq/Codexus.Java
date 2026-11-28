package codexus.base12111.plugin.utils.patch.codec;

import io.netty.buffer.ByteBuf;
public record EntityDataSerializerRef<T>(IStreamCodec<ByteBuf,T> codec) implements IEntityDataSerializer<T> {}
