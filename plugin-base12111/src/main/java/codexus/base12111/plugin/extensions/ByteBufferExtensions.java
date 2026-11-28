package codexus.base12111.plugin.extensions;

import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import codexus.base12111.plugin.utils.nbt.NbtTagType;
import codexus.base12111.plugin.utils.minecraft.DataComponentType;
import codexus.development.sdk.extensions.NettyExtensions;
public final class ByteBufferExtensions
{
    private ByteBufferExtensions() {}
    public static int nbtLength(ByteBuf buffer) { return nbtLength(buffer, false); }
	private static final int PackedBlockPositionLength = 8;

	public static int nbtLength(ByteBuf buffer, boolean withName)
	{
		int startIndex = buffer.readerIndex();
		try
		{
			NbtTagType rootType = NbtTagType.fromValue(buffer.readUnsignedByte());
			if (rootType == NbtTagType.TagEnd)
			{
				return 1;
			}

			if (withName)
			{
				skipUnsignedShortPrefixedBytes(buffer);
			}

			skipTagPayload(buffer, rootType);
			return buffer.readerIndex() - startIndex;
		}
		finally
		{
			buffer.readerIndex(startIndex);
		}
	}

	public static int itemStackLength(ByteBuf buffer)
	{
		int startIndex = buffer.readerIndex();
		try
		{
			skipDataComponentPatch(buffer);
			return buffer.readerIndex() - startIndex;
		}
		finally
		{
			buffer.readerIndex(startIndex);
		}
	}

	private static void skipTagPayload(ByteBuf buffer, NbtTagType type)
	{
		switch (type)
		{
			case TagEnd:
				return;
			case TagByte:
				buffer.skipBytes(1);
				return;
			case TagShort:
				buffer.skipBytes(2);
				return;
			case TagInt:
			case TagFloat:
				buffer.skipBytes(4);
				return;
			case TagLong:
			case TagDouble:
				buffer.skipBytes(8);
				return;
			case TagByteArray:
				skipNbtArray(buffer, 1);
				return;
			case TagString:
				skipUnsignedShortPrefixedBytes(buffer);
				return;
			case TagList:
				skipNbtList(buffer);
				return;
			case TagCompound:
				skipCompoundTagPayload(buffer);
				return;
			case TagIntArray:
				skipNbtArray(buffer, 4);
				return;
			case TagLongArray:
				skipNbtArray(buffer, 8);
				return;
			default:
				throw new IllegalArgumentException("Unknown NBT tag type: " + type + "");
		}
	}

	private static void skipCompoundTagPayload(ByteBuf buffer)
	{
		while (true)
		{
			NbtTagType childType = NbtTagType.fromValue(buffer.readUnsignedByte());
			if (childType == NbtTagType.TagEnd)
			{
				return;
			}

			skipUnsignedShortPrefixedBytes(buffer);
			skipTagPayload(buffer, childType);
		}
	}

	private static void skipNbtList(ByteBuf buffer)
	{
		NbtTagType elementType = NbtTagType.fromValue(buffer.readUnsignedByte());
		int count = buffer.readInt();
		if (count < 0)
		{
			throw new IllegalArgumentException("NBT list length cannot be negative: " + count + "");
		}

		for (int index = 0; index < count; index++)
		{
			skipTagPayload(buffer, elementType);
		}
	}

	private static void skipNbtArray(ByteBuf buffer, int elementSize)
	{
		int count = buffer.readInt();
		if (count < 0)
		{
			throw new IllegalArgumentException("NBT array length cannot be negative: " + count + "");
		}

		buffer.skipBytes(Math.multiplyExact(count, elementSize));
	}

	private static void skipUnsignedShortPrefixedBytes(ByteBuf buffer)
	{
		buffer.skipBytes(buffer.readUnsignedShort());
	}

	private static void skipDataComponentPatch(ByteBuf buffer)
	{
		int addedComponentCount = readCount(buffer, "added component");
		int removedComponentCount = readCount(buffer, "removed component");

		for (int index = 0; index < addedComponentCount; index++)
		{
			DataComponentType componentType = DataComponentType.fromValue(readVarInt(buffer));
			skipDataComponent(buffer, componentType);
		}

		for (int index = 0; index < removedComponentCount; index++)
		{
			readVarInt(buffer);
		}
	}

    private static void skipDataComponent(ByteBuf buffer, DataComponentType type) {
        switch (type) {
            case CustomData, CustomName, ItemName, IntangibleProjectile, MapDecorations,
                    DebugStickState, BucketEntityData, Recipes, Lock, ContainerLoot -> skipNbt(buffer);
            case MaxStackSize, MaxDamage, Damage, Rarity, RepairCost, MapId, MapPostProcessing,
                    Enchantable, OminousBottleAmplifier, BaseColor, VillagerVariant, WolfVariant,
                    WolfSoundVariant, WolfCollar, FoxVariant, SalmonSize, ParrotVariant,
                    TropicalFishPattern, TropicalFishBaseColor, TropicalFishPatternColor,
                    MooshroomVariant, RabbitVariant, PigVariant, CowVariant, FrogVariant, HorseVariant, LlamaVariant, AxolotlVariant,
                    CatVariant, CatCollar, SheepColor, ShulkerColor -> readVarInt(buffer);
            case Unbreakable, CreativeSlotLock, Glider -> { }
            case EnchantmentGlintOverride -> buffer.readBoolean();
            case ItemModel, TooltipStyle, DamageResistant, NoteBlockSound, ProvidesBannerPatterns -> skipString(buffer);
            case DyedColor, MapColor, MinimumAttackCharge, PotionDurationScale -> buffer.skipBytes(4);
            case UseEffects -> buffer.skipBytes(6);
            case Lore -> skipArray(buffer, ByteBufferExtensions::skipNbt);
            case Enchantments, StoredEnchantments -> skipArray(buffer, b -> { readVarInt(b); readVarInt(b); });
            case CanPlaceOn, CanBreak -> skipArray(buffer, ByteBufferExtensions::skipBlockPredicate);
            case AttributeModifiers -> skipArray(buffer, b -> {
                readVarInt(b); skipString(b); b.skipBytes(8); readVarInt(b); readVarInt(b);
                int display = readVarInt(b);
                if (display == 2) skipNbt(b);
                else if (display != 0 && display != 1) throw new UnsupportedOperationException("Unknown attribute display " + display);
            });
            case CustomModelData -> {
                skipArray(buffer, b -> b.skipBytes(4)); skipArray(buffer, b -> b.skipBytes(1));
                skipArray(buffer, ByteBufferExtensions::skipString); skipArray(buffer, b -> b.skipBytes(4));
            }
            case TooltipDisplay -> { buffer.readBoolean(); skipArray(buffer, b -> readVarInt(b)); }
            case Food -> { readVarInt(buffer); buffer.skipBytes(5); }
            case Consumable -> {
                buffer.skipBytes(4); readVarInt(buffer); skipSoundHolder(buffer); buffer.readBoolean();
                skipArray(buffer, ByteBufferExtensions::skipConsumeEffect);
            }
            case UseCooldown -> { buffer.skipBytes(4); skipOptional(buffer, ByteBufferExtensions::skipString); }
            case Tool -> {
                skipArray(buffer, b -> {
                    skipRegistryEntryHolderSet(b); skipOptional(b, v -> v.skipBytes(4));
                    skipOptional(b, ByteBuf::readBoolean);
                });
                buffer.skipBytes(4); readVarInt(buffer); buffer.readBoolean();
            }
            case Weapon -> { readVarInt(buffer); buffer.skipBytes(4); }
            case AttackRange -> buffer.skipBytes(24);
            case Repairable -> skipRegistryEntryHolderSet(buffer);
            case Equippable -> {
                readVarInt(buffer); skipSoundHolder(buffer);
                skipOptional(buffer, ByteBufferExtensions::skipString);
                skipOptional(buffer, ByteBufferExtensions::skipString);
                skipOptional(buffer, ByteBufferExtensions::skipRegistryEntryHolderSet);
                buffer.skipBytes(5); skipSoundHolder(buffer);
            }
            case DeathProtection -> skipArray(buffer, ByteBufferExtensions::skipConsumeEffect);
            case BlocksAttacks -> {
                buffer.skipBytes(8);
                skipArray(buffer, b -> { b.skipBytes(4); skipOptional(b, ByteBufferExtensions::skipRegistryEntryHolderSet); b.skipBytes(8); });
                buffer.skipBytes(12); skipOptional(buffer, ByteBufferExtensions::skipString);
                skipOptional(buffer, ByteBufferExtensions::skipSoundHolder);
                skipOptional(buffer, ByteBufferExtensions::skipSoundHolder);
            }
            case PiercingWeapon -> {
                buffer.skipBytes(2); skipOptional(buffer, ByteBufferExtensions::skipSoundHolder);
                skipOptional(buffer, ByteBufferExtensions::skipSoundHolder);
            }
            case KineticWeapon -> {
                readVarInt(buffer); readVarInt(buffer);
                for (int i = 0; i < 3; i++) skipOptional(buffer, b -> { readVarInt(b); b.skipBytes(8); });
                buffer.skipBytes(8); skipOptional(buffer, ByteBufferExtensions::skipSoundHolder);
                skipOptional(buffer, ByteBufferExtensions::skipSoundHolder);
            }
            case SwingAnimation -> { readVarInt(buffer); readVarInt(buffer); }
            case ChargedProjectiles, BundleContents, Container -> skipArray(buffer, ByteBufferExtensions::skipItemStack);
            case UseRemainder -> skipItemStack(buffer);
            case PotionContents -> {
                skipOptional(buffer, b -> readVarInt(b)); skipOptional(buffer, b -> b.skipBytes(4));
                skipArray(buffer, ByteBufferExtensions::skipPotionEffect);
                skipOptional(buffer, ByteBufferExtensions::skipString);
            }
            case SuspiciousStewEffects -> skipArray(buffer, b -> { readVarInt(b); readVarInt(b); });
            case WritableBookContent -> skipArray(buffer, b -> { skipString(b); skipOptional(b, ByteBufferExtensions::skipString); });
            case WrittenBookContent -> {
                skipString(buffer); skipOptional(buffer, ByteBufferExtensions::skipString);
                skipString(buffer); readVarInt(buffer);
                skipArray(buffer, b -> { skipNbt(b); skipOptional(b, ByteBufferExtensions::skipNbt); });
                buffer.readBoolean();
            }
            case Trim -> {
                skipRegistryEntryHolder(buffer, ByteBufferExtensions::skipTrimMaterial);
                skipRegistryEntryHolder(buffer, b -> { skipString(b); skipNbt(b); b.readBoolean(); });
            }
            case EntityData, BlockEntityData -> { readVarInt(buffer); skipNbt(buffer); }
            case Instrument -> skipEitherHolder(buffer, b -> { skipSoundHolder(b); b.skipBytes(8); skipNbt(b); });
            case ProvidesTrimMaterial -> skipEitherHolder(buffer, ByteBufferExtensions::skipTrimMaterial);
            case JukeboxPlayable -> skipEitherHolder(buffer, b -> { skipSoundHolder(b); skipNbt(b); b.skipBytes(4); readVarInt(b); });
            case LodestoneTracker -> {
                skipOptional(buffer, b -> { skipString(b); b.skipBytes(PackedBlockPositionLength); });
                buffer.readBoolean();
            }
            case FireworkExplosion -> skipFireworkExplosion(buffer);
            case Fireworks -> { readVarInt(buffer); skipArray(buffer, ByteBufferExtensions::skipFireworkExplosion); }
            case Profile -> new codexus.base12111.plugin.utils.patch.codec.impls.ResolvableProfileCodec().decode(buffer);
            case BannerPatterns -> skipArray(buffer, b -> {
                skipRegistryEntryHolder(b, v -> { skipString(v); skipString(v); }); readVarInt(b);
            });
            case PotDecorations -> skipArray(buffer, b -> readVarInt(b));
            case BlockState -> skipArray(buffer, b -> { skipString(b); skipString(b); });
            case Bees -> skipArray(buffer, b -> { readVarInt(b); skipNbt(b); readVarInt(b); readVarInt(b); });
            case BreakSound -> skipSoundHolder(buffer);
            case PaintingVariant -> skipPaintingVariant(buffer);
            case DamageType, ChickenVariant, ZombieNautilusVariant -> {
                if (buffer.readBoolean()) readVarInt(buffer); else skipString(buffer);
            }
        }
    }

    public static void skipPaintingVariant(ByteBuf buffer) {
        skipRegistryEntryHolder(buffer, b -> {
            readVarInt(b); readVarInt(b); skipString(b);
            skipOptional(b, ByteBufferExtensions::skipNbt); skipOptional(b, ByteBufferExtensions::skipNbt);
        });
    }

    private static void skipBlockPredicate(ByteBuf buffer) {
        skipOptional(buffer, ByteBufferExtensions::skipRegistryEntryHolderSet);
        skipOptional(buffer, b -> skipArray(b, v -> {
            skipString(v);
            if (v.readBoolean()) skipString(v);
            else { skipOptional(v, ByteBufferExtensions::skipString); skipOptional(v, ByteBufferExtensions::skipString); }
        }));
        skipOptional(buffer, ByteBufferExtensions::skipNbt);
        // Exact component predicates contain typed values, not a component patch.
        skipArray(buffer, b -> skipDataComponent(b, DataComponentType.fromValue(readVarInt(b))));
        // Partial predicates use a registry-or-component discriminator followed by an NBT codec value.
        skipArray(buffer, b -> { b.readBoolean(); readVarInt(b); skipNbt(b); });
    }

    private static void skipConsumeEffect(ByteBuf buffer) {
        int type = readVarInt(buffer);
        switch (type) {
            case 0 -> { skipArray(buffer, ByteBufferExtensions::skipPotionEffect); buffer.skipBytes(4); }
            case 1 -> skipRegistryEntryHolderSet(buffer);
            case 2 -> { }
            case 3 -> buffer.skipBytes(4);
            case 4 -> skipSoundHolder(buffer);
            default -> throw new UnsupportedOperationException("Unknown consume effect " + type);
        }
    }

    private static void skipPotionEffect(ByteBuf buffer) {
        readVarInt(buffer); skipPotionEffectDetails(buffer);
    }

    private static void skipPotionEffectDetails(ByteBuf buffer) {
        do { readVarInt(buffer); readVarInt(buffer); buffer.skipBytes(3); } while (buffer.readBoolean());
    }

    private static void skipTrimMaterial(ByteBuf buffer) {
        skipString(buffer); skipArray(buffer, b -> { skipString(b); skipString(b); }); skipNbt(buffer);
    }

    private static void skipEitherHolder(ByteBuf buffer, Consumer<ByteBuf> skipInline) {
        if (buffer.readBoolean()) skipRegistryEntryHolder(buffer, skipInline);
        else skipString(buffer);
    }

    private static void skipSoundHolder(ByteBuf buffer) {
        skipRegistryEntryHolder(buffer, b -> { skipString(b); skipOptional(b, v -> v.skipBytes(4)); });
    }

    private static void skipFireworkExplosion(ByteBuf buffer) {
        readVarInt(buffer); skipFixedSizeArray(buffer, 4); skipFixedSizeArray(buffer, 4); buffer.skipBytes(2);
    }

    private static void skipItemStack(ByteBuf buffer) {
        int count = readCount(buffer, "item");
        if (count == 0) return;
        readVarInt(buffer); skipDataComponentPatch(buffer);
    }

	private static void skipRegistryEntryHolder(ByteBuf buffer, Consumer<ByteBuf> skipInlineValue)
	{
		if (readVarInt(buffer) == 0)
		{
			skipInlineValue.accept(buffer);
		}
	}

	private static void skipRegistryEntryHolderSet(ByteBuf buffer)
	{
		int encodedCount = readVarInt(buffer);
		if (encodedCount == 0)
		{
			skipString(buffer);
			return;
		}
		if (encodedCount < 0)
		{
			throw new IllegalArgumentException("Registry holder set length cannot be negative: " + encodedCount + "");
		}

		for (int index = 0; index < encodedCount - 1; index++)
		{
			readVarInt(buffer);
		}
	}

	private static void skipNbt(ByteBuf buffer)
	{
		buffer.skipBytes(nbtLength(buffer));
	}

	private static void skipString(ByteBuf buffer)
	{
		int byteCount = readCount(buffer, "String byte");
		buffer.skipBytes(byteCount);
	}

	private static void skipOptional(ByteBuf buffer, Consumer<ByteBuf> skipValue)
	{
		if (buffer.readBoolean())
		{
			skipValue.accept(buffer);
		}
	}

	private static void skipArray(ByteBuf buffer, Consumer<ByteBuf> skipElement)
	{
		int count = readCount(buffer, "array element");
		for (int index = 0; index < count; index++)
		{
			skipElement.accept(buffer);
		}
	}

	private static void skipFixedSizeArray(ByteBuf buffer, int elementSize)
	{
		int count = readCount(buffer, "array element");
		buffer.skipBytes(Math.multiplyExact(count, elementSize));
	}

	private static int readCount(ByteBuf buffer, String description)
	{
		int count = readVarInt(buffer);
		if (count < 0)
		{
			throw new IllegalArgumentException("" + description + " count cannot be negative: " + count + "");
		}

		return count;
	}

	private static int readVarInt(ByteBuf buffer)
	{
		return NettyExtensions.readVarIntFromBuffer(buffer);
	}
}
