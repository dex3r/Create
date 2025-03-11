package com.simibubi.create.foundation.mixin.fabric.infra;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.infrastructure.fabric.block.SecondaryUseBypassingBlock;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
	@ModifyExpressionValue(
		method = "performUseItemOn",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/player/LocalPlayer;isSecondaryUseActive()Z"
		)
	)
	private boolean maybeBypassSecondaryUse(boolean original, LocalPlayer player, InteractionHand hand, BlockHitResult hit) {
		if (!original)
			return false;

		BlockState state = player.level().getBlockState(hit.getBlockPos());
		if (state.getBlock() instanceof SecondaryUseBypassingBlock block && block.shouldBypassSecondaryUse(player, hand, state)) {
			return false;
		}

		return true;
	}
}
