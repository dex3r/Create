package com.simibubi.create.foundation.mixin.fabric.infra.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.infrastructure.fabric.block.WeakPowerCheckingBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.SignalGetter;

import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SignalGetter.class)
public interface SignalGetterMixin {
	@WrapOperation(
		method = "getSignal",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/state/BlockState;isRedstoneConductor(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z"
		)
	)
	private boolean checkWeakPower(BlockState state, BlockGetter level, BlockPos pos, Operation<Boolean> original,
								   @Local(argsOnly = true) Direction side) {
		if (state.getBlock() instanceof WeakPowerCheckingBlock block) {
			return block.shouldCheckWeakPower(state, (SignalGetter) this, pos, side);
		}

		return original.call(state, level, pos);
	}
}
