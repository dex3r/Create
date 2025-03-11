package com.simibubi.create.foundation.mixin.fabric.infra;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.infrastructure.fabric.block.SecondaryUseBypassingBlock;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
	@ModifyExpressionValue(
		method = "useItemOn",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerPlayer;isSecondaryUseActive()Z"
		)
	)
	private boolean maybeBypassSecondaryUse(boolean original, ServerPlayer player, Level level, ItemStack stack, InteractionHand hand, BlockHitResult hit) {
		if (!original)
			return false;

		BlockState state = player.level().getBlockState(hit.getBlockPos());
		if (state.getBlock() instanceof SecondaryUseBypassingBlock block && block.shouldBypassSecondaryUse(player, hand, state)) {
			return false;
		}

		return true;
	}
}
