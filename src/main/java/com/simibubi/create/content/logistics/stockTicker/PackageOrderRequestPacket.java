package com.simibubi.create.content.logistics.stockTicker;

import com.simibubi.create.AllPackets;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour.RequestType;
import com.simibubi.create.content.logistics.packagerLink.WiFiEffectPacket;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterBlock;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

public class PackageOrderRequestPacket extends BlockEntityConfigurationPacket<StockTickerBlockEntity> {
	public static final StreamCodec<RegistryFriendlyByteBuf, PackageOrderRequestPacket> STREAM_CODEC = StreamCodec.composite(
	    BlockPos.STREAM_CODEC, packet -> packet.pos,
		PackageOrder.STREAM_CODEC, packet -> packet.order,
		ByteBufCodecs.STRING_UTF8, packet -> packet.address,
		ByteBufCodecs.BOOL, packet -> packet.encodeRequester,
		PackageOrder.STREAM_CODEC, packet -> packet.craftingRequest,
	    PackageOrderRequestPacket::new
	);

	private final PackageOrder order;
	private final String address;
	private final boolean encodeRequester;
	private final PackageOrder craftingRequest;

	public PackageOrderRequestPacket(BlockPos pos, PackageOrder order, String address, boolean encodeRequester, PackageOrder craftingRequest) {
		super(pos);
		this.order = order;
		this.address = address;
		this.encodeRequester = encodeRequester;
		this.craftingRequest = craftingRequest;
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.LOGISTICS_PACKAGE_REQUEST;
	}

	@Override
	protected void applySettings(ServerPlayer player, StockTickerBlockEntity be) {
		if (encodeRequester) {
			if (!order.isEmpty())
				AllSoundEvents.CONFIRM.playOnServer(be.getLevel(), pos);
			player.closeContainer();
			RedstoneRequesterBlock.programRequester(player, be, order, address, craftingRequest);
			return;
		}

		if (!order.isEmpty()) {
			AllSoundEvents.STOCK_TICKER_REQUEST.playOnServer(be.getLevel(), pos);
			AllAdvancements.STOCK_TICKER.awardTo(player);
			WiFiEffectPacket.send(player.level(), pos);
		}

		be.broadcastPackageRequest(RequestType.PLAYER, order, null, address, craftingRequest.isEmpty() ? null : craftingRequest);
	}
}
