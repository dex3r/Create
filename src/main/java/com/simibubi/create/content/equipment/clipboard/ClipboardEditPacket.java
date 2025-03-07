package com.simibubi.create.content.equipment.clipboard;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllPackets;
import com.simibubi.create.foundation.utility.CreateComponentProcessors;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record ClipboardEditPacket(int hotbarSlot, DataComponentPatch dataComponentPatch, @Nullable BlockPos targetedBlock) implements ServerboundPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, ClipboardEditPacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, ClipboardEditPacket::hotbarSlot,
		DataComponentPatch.STREAM_CODEC, ClipboardEditPacket::dataComponentPatch,
		CatnipStreamCodecBuilders.nullable(BlockPos.STREAM_CODEC), ClipboardEditPacket::targetedBlock,
		ClipboardEditPacket::new
	);

	@Override
	public void handle(ServerPlayer sender) {
		DataComponentPatch processedData = CreateComponentProcessors.clipboardProcessor(dataComponentPatch);

		if (targetedBlock != null) {
			Level world = sender.level();
			if (!world.isLoaded(targetedBlock))
				return;
			if (!targetedBlock.closerThan(sender.blockPosition(), 20))
				return;
			if (world.getBlockEntity(targetedBlock) instanceof ClipboardBlockEntity cbe) {
				if (processedData.isEmpty()) {
					clearComponents(cbe.dataContainer);
				} else {
					cbe.dataContainer.remove(AllDataComponents.CLIPBOARD_PREVIOUSLY_OPENED_PAGE);
					cbe.dataContainer.applyComponents(processedData);
				}
				cbe.onEditedBy(sender);
			}
			return;
		}

		ItemStack itemStack = sender.getInventory()
				.getItem(hotbarSlot);
		if (!AllBlocks.CLIPBOARD.isIn(itemStack))
			return;
		if (processedData.isEmpty()) {
			clearComponents(itemStack);
		} else {
			itemStack.remove(AllDataComponents.CLIPBOARD_PREVIOUSLY_OPENED_PAGE);
			itemStack.applyComponents(processedData);
		}
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CLIPBOARD_EDIT;
	}

	private static void clearComponents(ItemStack stack) {
		stack.remove(AllDataComponents.CLIPBOARD_TYPE);
		stack.remove(AllDataComponents.CLIPBOARD_PAGES);
		stack.remove(AllDataComponents.CLIPBOARD_READ_ONLY);
		stack.remove(AllDataComponents.CLIPBOARD_COPIED_VALUES);
		stack.remove(AllDataComponents.CLIPBOARD_PREVIOUSLY_OPENED_PAGE);
	}
}
