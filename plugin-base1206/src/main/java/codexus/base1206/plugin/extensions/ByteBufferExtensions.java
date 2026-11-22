package codexus.base1206.plugin.extensions;

import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import codexus.base1206.plugin.utils.nbt.NbtTagType;
import codexus.base1206.plugin.utils.minecraft.DataComponentType;
import codexus.development.sdk.extensions.NettyExtensions;
public final class ByteBufferExtensions
{
    private ByteBufferExtensions() {}
    public static int nbtLength(ByteBuf buffer) { return nbtLength(buffer, false); }
	private static final int UuidLength = 16;
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

	private static void skipDataComponent(ByteBuf buffer, DataComponentType type)
	{
		switch (type)
		{
			case CustomData:
			case CustomName:
			case ItemName:
			case IntangibleProjectile:
			case MapDecorations:
			case DebugStickState:
			case EntityData:
			case BucketEntityData:
			case BlockEntityData:
			case Recipes:
			case Lock:
			case ContainerLoot:
				skipNbt(buffer);
				return;
			case MaxStackSize:
			case MaxDamage:
			case Damage:
			case Rarity:
			case CustomModelData:
			case RepairCost:
			case MapId:
			case MapPostProcessing:
			case OminousBottleAmplifier:
			case BaseColor:
				readVarInt(buffer);
				return;
			case Unbreakable:
			case EnchantmentGlintOverride:
				buffer.readBoolean();
				return;
			case Lore:
				skipArray(buffer, ByteBufferExtensions::skipNbt);
				return;
			case Enchantments:
			case StoredEnchantments:
				skipEnchantments(buffer);
				return;
			case CanPlaceOn:
			case CanBreak:
				skipAdventureModePredicate(buffer);
				return;
			case AttributeModifiers:
				skipAttributeModifiers(buffer);
				return;
			case HideAdditionalTooltip:
			case HideTooltip:
			case CreativeSlotLock:
			case FireResistant:
				return;
			case Food:
				skipFoodProperties(buffer);
				return;
			case Tool:
				skipToolProperties(buffer);
				return;
			case DyedColor:
				buffer.skipBytes(4);
				buffer.readBoolean();
				return;
			case MapColor:
				buffer.skipBytes(4);
				return;
			case ChargedProjectiles:
			case BundleContents:
			case Container:
				skipArray(buffer, ByteBufferExtensions::skipItemStack);
				return;
			case PotionContents:
				skipPotionContents(buffer);
				return;
			case SuspiciousStewEffects:
				skipArray(buffer, ByteBufferExtensions::skipSuspiciousStewEffect);
				return;
			case WritableBookContent:
				skipArray(buffer, ByteBufferExtensions::skipWritableBookPage);
				return;
			case WrittenBookContent:
				skipWrittenBookContent(buffer);
				return;
			case Trim:
				skipArmorTrim(buffer);
				return;
			case Instrument:
				skipRegistryEntryHolder(buffer, ByteBufferExtensions::skipInstrumentData);
				return;
			case LodestoneTracker:
				skipLodestoneTracker(buffer);
				return;
			case FireworkExplosion:
				skipFireworkExplosion(buffer);
				return;
			case Fireworks:
				skipFireworks(buffer);
				return;
			case Profile:
				skipGameProfile(buffer);
				return;
			case NoteBlockSound:
				skipString(buffer);
				return;
			case BannerPatterns:
				skipArray(buffer, ByteBufferExtensions::skipBannerPatternLayer);
				return;
			case PotDecorations:
				skipArray(buffer, value -> readVarInt(value));
				return;
			case BlockState:
				skipArray(buffer, ByteBufferExtensions::skipStringPair);
				return;
			case Bees:
				skipArray(buffer, ByteBufferExtensions::skipBeeData);
				return;
			default:
				throw new UnsupportedOperationException("Unsupported data component type id: " + type.value() + "");
		}
	}

	private static void skipEnchantments(ByteBuf buffer)
	{
		skipArray(buffer, value ->
		{
			readVarInt(value);
			readVarInt(value);
		});
		buffer.readBoolean();
	}

	private static void skipAdventureModePredicate(ByteBuf buffer)
	{
		skipArray(buffer, ByteBufferExtensions::skipBlockPredicate);
		buffer.readBoolean();
	}

	private static void skipBlockPredicate(ByteBuf buffer)
	{
		skipOptional(buffer, ByteBufferExtensions::skipRegistryEntryHolderSet);
		skipOptional(buffer, value -> skipArray(value, ByteBufferExtensions::skipBlockProperty));
		skipNbt(buffer);
	}

	private static void skipBlockProperty(ByteBuf buffer)
	{
		skipString(buffer);
		if (buffer.readBoolean())
		{
			skipString(buffer);
			return;
		}

		skipString(buffer);
		skipString(buffer);
	}

	private static void skipAttributeModifiers(ByteBuf buffer)
	{
		skipArray(buffer, value ->
		{
			readVarInt(value);
			value.skipBytes(UuidLength);
			skipString(value);
			value.skipBytes(8);
			readVarInt(value);
			readVarInt(value);
		});
		buffer.readBoolean();
	}

	private static void skipFoodProperties(ByteBuf buffer)
	{
		readVarInt(buffer);
		buffer.skipBytes(4);
		buffer.readBoolean();
		buffer.skipBytes(4);
		skipItemStack(buffer);
		skipArray(buffer, value ->
		{
			readVarInt(value);
			value.skipBytes(4);
		});
	}

	private static void skipToolProperties(ByteBuf buffer)
	{
		skipArray(buffer, value ->
		{
			skipRegistryEntryHolderSet(value);
			skipOptional(value, optionalValue -> optionalValue.skipBytes(4));
			skipOptional(value, optionalValue -> optionalValue.readBoolean());
		});
		buffer.skipBytes(4);
		readVarInt(buffer);
	}

	private static void skipPotionContents(ByteBuf buffer)
	{
		skipOptional(buffer, value -> readVarInt(value));
		skipOptional(buffer, value -> value.skipBytes(4));
		skipArray(buffer, ByteBufferExtensions::skipPotionEffect);
		skipOptional(buffer, ByteBufferExtensions::skipString);
	}

	private static void skipPotionEffect(ByteBuf buffer)
	{
		readVarInt(buffer);
		skipPotionEffectDetails(buffer);
	}

	private static void skipPotionEffectDetails(ByteBuf buffer)
	{
		readVarInt(buffer);
		readVarInt(buffer);
		buffer.skipBytes(3);
		skipOptional(buffer, ByteBufferExtensions::skipPotionEffectDetails);
	}

	private static void skipSuspiciousStewEffect(ByteBuf buffer)
	{
		readVarInt(buffer);
		readVarInt(buffer);
	}

	private static void skipWritableBookPage(ByteBuf buffer)
	{
		skipString(buffer);
		skipOptional(buffer, ByteBufferExtensions::skipString);
	}

	private static void skipWrittenBookContent(ByteBuf buffer)
	{
		skipString(buffer);
		skipOptional(buffer, ByteBufferExtensions::skipString);
		skipString(buffer);
		readVarInt(buffer);
		skipArray(buffer, value ->
		{
			skipNbt(value);
			skipNbt(value);
		});
		buffer.readBoolean();
	}

	private static void skipArmorTrim(ByteBuf buffer)
	{
		skipRegistryEntryHolder(buffer, ByteBufferExtensions::skipArmorTrimMaterial);
		skipRegistryEntryHolder(buffer, ByteBufferExtensions::skipArmorTrimPattern);
		buffer.readBoolean();
	}

	private static void skipArmorTrimMaterial(ByteBuf buffer)
	{
		skipString(buffer);
		readVarInt(buffer);
		skipArray(buffer, ByteBufferExtensions::skipStringPair);
		skipNbt(buffer);
	}

	private static void skipArmorTrimPattern(ByteBuf buffer)
	{
		skipString(buffer);
		readVarInt(buffer);
		skipNbt(buffer);
		buffer.readBoolean();
	}

	private static void skipInstrumentData(ByteBuf buffer)
	{
		skipRegistryEntryHolder(buffer, ByteBufferExtensions::skipSoundEvent);
		buffer.skipBytes(4 * 2);
		skipNbt(buffer);
	}

	private static void skipSoundEvent(ByteBuf buffer)
	{
		skipString(buffer);
		skipOptional(buffer, value -> value.skipBytes(4));
	}

	private static void skipLodestoneTracker(ByteBuf buffer)
	{
		skipOptional(buffer, value ->
		{
			skipString(value);
			value.skipBytes(PackedBlockPositionLength);
		});
		buffer.readBoolean();
	}

	private static void skipFireworkExplosion(ByteBuf buffer)
	{
		readVarInt(buffer);
		skipFixedSizeArray(buffer, 4);
		skipFixedSizeArray(buffer, 4);
		buffer.skipBytes(2);
	}

	private static void skipFireworks(ByteBuf buffer)
	{
		readVarInt(buffer);
		skipArray(buffer, ByteBufferExtensions::skipFireworkExplosion);
	}

	private static void skipGameProfile(ByteBuf buffer)
	{
		skipOptional(buffer, ByteBufferExtensions::skipString);
		skipOptional(buffer, value -> value.skipBytes(UuidLength));
		skipArray(buffer, value ->
		{
			skipString(value);
			skipString(value);
			skipOptional(value, ByteBufferExtensions::skipString);
		});
	}

	private static void skipBannerPatternLayer(ByteBuf buffer)
	{
		skipRegistryEntryHolder(buffer, value ->
		{
			skipString(value);
			skipString(value);
		});
		readVarInt(buffer);
	}

	private static void skipBeeData(ByteBuf buffer)
	{
		skipNbt(buffer);
		readVarInt(buffer);
		readVarInt(buffer);
	}

	private static void skipStringPair(ByteBuf buffer)
	{
		skipString(buffer);
		skipString(buffer);
	}

	private static void skipItemStack(ByteBuf buffer)
	{
		int itemCount = buffer.readUnsignedByte();
		if (itemCount == 0)
		{
			return;
		}

		readVarInt(buffer);
		skipDataComponentPatch(buffer);
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
