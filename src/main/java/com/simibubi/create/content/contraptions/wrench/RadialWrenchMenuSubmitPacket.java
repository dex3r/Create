package com.simibubi.create.content.contraptions.wrench;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class RadialWrenchMenuSubmitPacket extends SimplePacketBase {

	private final BlockPos blockPos;
	private final BlockState newState;

	public RadialWrenchMenuSubmitPacket(BlockPos blockPos, BlockState newState) {
		this.blockPos = blockPos;
		this.newState = newState;
	}

	public RadialWrenchMenuSubmitPacket(FriendlyByteBuf buffer) {
		this.blockPos = buffer.readBlockPos();
		this.newState = buffer.readById(Block.BLOCK_STATE_REGISTRY);
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(blockPos);
		buffer.writeId(Block.BLOCK_STATE_REGISTRY, newState);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			Level level = player.level();

			if (!level.getBlockState(blockPos).is(newState.getBlock()))
				return;
			
			BlockState updatedState = Block.updateFromNeighbourShapes(newState, level, blockPos);
			KineticBlockEntity.switchToBlockState(level, blockPos, updatedState);

			IWrenchable.playRotateSound(level, blockPos);
		});
		return true;
	}
}
