package com.simibubi.create.infrastructure.fabric.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface WeakPowerCheckingBlock {
	boolean shouldCheckWeakPower(BlockState state, SignalGetter level, BlockPos pos, Direction side);
}
