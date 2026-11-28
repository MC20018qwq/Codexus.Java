package codexus.base12111.plugin.utils.patch.codec;

public interface IStreamCodec<TBuffer, TValue> {
    TValue decode(TBuffer buffer);
    void encode(TBuffer buffer, TValue value);
}
