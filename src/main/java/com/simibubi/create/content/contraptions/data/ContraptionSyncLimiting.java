package com.simibubi.create.content.contraptions.data;

import com.simibubi.create.compat.Mods;

import io.netty.buffer.Unpooled;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class ContraptionSyncLimiting {
	/**
	 * Contraption entity sync is limited by the clientbound custom payload limit, since that's what Forge's
	 * extended spawn packet uses. The NBT limit is irrelevant since it's bypassed on deserialization.
	 */
	public static final int SIZE_LIMIT = 1_048_576;

	// increased packet limits provided by other mods.
	public static final int PACKET_FIXER_LIMIT = SIZE_LIMIT * 100;
	public static final int XL_PACKETS_LIMIT = Integer.MAX_VALUE;

	// leave some room for the rest of the packet.
	public static final int BUFFER = 20_000;

	// the actual limit to be used
	public static final int LIMIT = Util.make(() -> {
		// the smallest limit needs to be used, as we can't guarantee that all mixins are applied if multiple are present.
		if (Mods.PACKETFIXER.isLoaded()) {
			return PACKET_FIXER_LIMIT;
		} else if (Mods.XLPACKETS.isLoaded()) {
			return XL_PACKETS_LIMIT;
		}

		// none are present, use vanilla default
		return SIZE_LIMIT;
	}) - BUFFER;

	/**
	 * @return true if the given NBT is too large for a contraption to be synced to clients.
	 */
	public static boolean isTooLargeForSync(CompoundTag data) {
		return byteSize(data) > LIMIT;
	}

	/**
	 * @return the size of the given NBT when encoded, in bytes
	 */
	private static long byteSize(CompoundTag data) {
		FriendlyByteBuf test = new FriendlyByteBuf(Unpooled.buffer());
		test.writeNbt(data);
		return test.writerIndex();
	}
}
