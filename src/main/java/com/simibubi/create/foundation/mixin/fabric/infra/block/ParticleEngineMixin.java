package com.simibubi.create.foundation.mixin.fabric.infra.block;

import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.AllTags.AllBlockTags;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
	@Shadow
	protected ClientLevel level;

	@Unique
	@Nullable
	private Double particleChance = null;

	@WrapOperation(
		method = "destroy",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/phys/shapes/VoxelShape;forAllBoxes(Lnet/minecraft/world/phys/shapes/Shapes$DoubleLineConsumer;)V"
		)
	)
	private void reduceDestroyEffects(VoxelShape shape, Shapes.DoubleLineConsumer action, Operation<Void> original,
									  @Local(argsOnly = true) BlockState state) {
		if (AllBlockTags.HAS_REDUCED_DESTROY_EFFECTS.matches(state)) {
			MutableInt boxCount = new MutableInt(0);
			shape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> boxCount.increment());
			this.particleChance = 1d / boxCount.getValue();
		}

		try {
			original.call(shape, action);
		} finally {
			this.particleChance = null;
		}
	}

	@WrapOperation(
		method = "method_34020",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/particle/ParticleEngine;add(Lnet/minecraft/client/particle/Particle;)V"
		)
	)
	private void maybeDontAdd(ParticleEngine self, Particle particle, Operation<Void> original) {
		if (this.particleChance != null && this.level.getRandom().nextDouble() > this.particleChance)
			return;

		original.call(self, particle);
	}
}
