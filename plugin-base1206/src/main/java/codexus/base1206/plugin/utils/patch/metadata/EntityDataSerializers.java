package codexus.base1206.plugin.utils.patch.metadata;

import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import codexus.base1206.plugin.utils.minecraft.*;
import codexus.base1206.plugin.utils.patch.IdBiMap;
import codexus.base1206.plugin.utils.patch.codec.*;
import codexus.base1206.plugin.utils.patch.codec.impls.*;
public final class EntityDataSerializers {
    private EntityDataSerializers() {}
    private static final IdBiMap<IEntityDataSerializer<?>> serializers=new IdBiMap<>();
    public static final IEntityDataSerializer<Integer> Byte=register(new ByteCodec());
    public static final IEntityDataSerializer<Integer> Int=register(new IntegerCodec());
    public static final IEntityDataSerializer<Long> Long=register(new LongCodec());
    public static final IEntityDataSerializer<Float> Float=register(new FloatCodec());
    public static final IEntityDataSerializer<String> StringSerializer=register(new StringCodec());
    public static final IEntityDataSerializer<Component> Component=register(new ComponentCodec());
    public static final IEntityDataSerializer<Optional<Component>> OptionalComponent=register(new OptionalComponentCodec());
    public static final IEntityDataSerializer<ItemStack> ItemStack=register(new ItemStackCodec());
    public static final IEntityDataSerializer<BlockState> BlockState=register(new BlockStateCodec());
    public static final IEntityDataSerializer<Optional<BlockState>> OptionalBlockState=register(new OptionalBlockStateCodec());
    public static final IEntityDataSerializer<Boolean> Boolean=register(new BooleanCodec());
    public static final IEntityDataSerializer<ParticleOptions> Particle=register(new ParticleCodec());
    public static final IEntityDataSerializer<List<ParticleOptions>> Particles=register(new ParticleListCodec());
    public static final IEntityDataSerializer<Rotations> Rotations=register(new RotationsCodec());
    public static final IEntityDataSerializer<BlockPos> BlockPos=register(new BlockPosCodec());
    public static final IEntityDataSerializer<Optional<BlockPos>> OptionalBlockPos=register(new OptionalBlockPosCodec());
    public static final IEntityDataSerializer<Direction> Direction=register(new DirectionCodec());
    public static final IEntityDataSerializer<Optional<byte[]>> OptionalUuid=register(new OptionalUuidCodec());
    public static final IEntityDataSerializer<Optional<GlobalPos>> OptionalGlobalPos=register(new OptionalGlobalPosCodec());
    public static final IEntityDataSerializer<CompoundTag> CompoundTag=register(new CompoundTagCodec());
    public static final IEntityDataSerializer<VillagerData> VillagerData=register(new VillagerDataCodec());
    public static final IEntityDataSerializer<OptionalInt> OptionalUnsignedInt=register(new OptionalUnsignedIntCodec());
    public static final IEntityDataSerializer<Pose> Pose=register(new PoseCodec());
    public static final IEntityDataSerializer<Holder<CatVariant>> CatVariant=register(new CatVariantCodec());
    public static final IEntityDataSerializer<Holder<WolfVariant>> WolfVariant=register(new WolfVariantCodec());
    public static final IEntityDataSerializer<Holder<FrogVariant>> FrogVariant=register(new FrogVariantCodec());
    public static final IEntityDataSerializer<Holder<PaintingVariant>> PaintingVariant=register(new PaintingVariantCodec());
    public static final IEntityDataSerializer<Armadillo.ArmadilloState> ArmadilloState=register(new ArmadilloStateCodec());
    public static final IEntityDataSerializer<Sniffer.State> SnifferState=register(new SnifferStateCodec());
    public static final IEntityDataSerializer<Vector3F> Vector3=register(new Vector3FCodec());
    public static final IEntityDataSerializer<Quaternionf> Quaternion=register(new QuaternionCodec());
    public static IEntityDataSerializer<?> getSerializer(int id) { try {return serializers.getObject(id);} catch(IndexOutOfBoundsException e){throw new UnsupportedOperationException("Unsupported entity metadata serializer id: "+id,e);} }
    public static int getSerializedId(IEntityDataSerializer<?> serializer) {return serializers.getId(serializer);}
    private static <T> IEntityDataSerializer<T> register(IStreamCodec<ByteBuf,T> codec) {IEntityDataSerializer<T> serializer=new EntityDataSerializerRef<>(codec);serializers.add(serializer);return serializer;}
}
