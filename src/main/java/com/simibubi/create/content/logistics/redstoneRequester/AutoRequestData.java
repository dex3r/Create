package com.simibubi.create.content.logistics.redstoneRequester;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record AutoRequestData(PackageOrder encodedRequest, PackageOrder encodedRequestContext,
							  String encodedTargetAddress, BlockPos targetOffset, String targetDim, boolean isValid) {
	public static final Codec<AutoRequestData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		PackageOrder.CODEC.fieldOf("encoded_request").forGetter(i -> i.encodedRequest),
		PackageOrder.CODEC.fieldOf("encoded_request_context").forGetter(i -> i.encodedRequestContext),
		Codec.STRING.fieldOf("encoded_target_address").forGetter(i -> i.encodedTargetAddress),
		BlockPos.CODEC.fieldOf("target_offset").forGetter(i -> i.targetOffset),
		Codec.STRING.fieldOf("target_dim").forGetter(i -> i.targetDim),
		Codec.BOOL.fieldOf("is_valid").forGetter(i -> i.isValid)
	).apply(instance, AutoRequestData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, AutoRequestData> STREAM_CODEC = StreamCodec.composite(
	    PackageOrder.STREAM_CODEC, i -> i.encodedRequest,
		PackageOrder.STREAM_CODEC, i -> i.encodedRequestContext,
		ByteBufCodecs.STRING_UTF8, i -> i.encodedTargetAddress,
	    BlockPos.STREAM_CODEC, i -> i.targetOffset,
	    ByteBufCodecs.STRING_UTF8, i -> i.targetDim,
	    ByteBufCodecs.BOOL, i -> i.isValid,
	    AutoRequestData::new
	);

	public AutoRequestData() {
		this(PackageOrder.empty(), PackageOrder.empty(), "", BlockPos.ZERO, "null", false);
	}

	public void writeToItem(BlockPos position, ItemStack itemStack) {
		Mutable mutable = new Mutable(this);
		mutable.targetOffset = position.offset(targetOffset);
		itemStack.set(AllDataComponents.AUTO_REQUEST_DATA, mutable.toImmutable());
	}

	public static AutoRequestData readFromItem(Level level, Player player, BlockPos position, ItemStack itemStack) {
		AutoRequestData requestData = itemStack.get(AllDataComponents.AUTO_REQUEST_DATA);
		if (requestData == null)
			return null;

		Mutable mutable = new Mutable(requestData);

		mutable.targetOffset = mutable.targetOffset.subtract(position);
		mutable.isValid =
			mutable.targetOffset.closerThan(BlockPos.ZERO, 128) && requestData.targetDim.equals(level.dimension()
				.location()
				.toString());

		if (player != null)
			CreateLang
				.translate(mutable.isValid ? "redstone_requester.keeper_connected"
					: "redstone_requester.keeper_too_far_away")
				.style(mutable.isValid ? ChatFormatting.WHITE : ChatFormatting.RED)
				.sendStatus(player);

		return mutable.toImmutable();
	}

	public static class Mutable {
		public PackageOrder encodedRequest = PackageOrder.empty();
		public PackageOrder encodedRequestContext = PackageOrder.empty();
		public String encodedTargetAddress = "";
		public BlockPos targetOffset = BlockPos.ZERO;
		public String targetDim = "null";
		public boolean isValid = false;

		public Mutable() {
		}

		public Mutable(AutoRequestData data) {
			encodedRequest = data.encodedRequest;
			encodedRequestContext = data.encodedRequestContext;
			encodedTargetAddress = data.encodedTargetAddress;
			targetOffset = data.targetOffset;
			targetDim = data.targetDim;
			isValid = data.isValid;
		}

		public AutoRequestData toImmutable() {
			return new AutoRequestData(encodedRequest, encodedRequestContext, encodedTargetAddress, targetOffset, targetDim, isValid);
		}
	}
}
