package codexus.base1206.plugin.utils.minecraft;

public class ParticleOptions {
    public int ParticleId;
    public byte[] RawData;
    public ParticleOptions(int particleId, byte[] rawData) {
        ParticleId = particleId;
        RawData = rawData;
    }
}
