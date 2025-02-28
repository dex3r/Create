package com.simibubi.create.compat.botania;

import com.simibubi.create.api.behaviour.spouting.BlockSpoutingBehaviour;
import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import vazkii.botania.api.block.PetalApothecary;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;

public enum ApothecaryFilling implements BlockSpoutingBehaviour {
	INSTANCE;

	@Override
	public long fillBlock(Level level, BlockPos pos, SpoutBlockEntity spout, FluidStack availableFluid, boolean simulate) {
		if (!enabled())
			return 0;

		BlockEntity be = level.getBlockEntity(pos);
		if (be == null)
			return 0;

		// this shouldn't fail but... better safe than sorry.
		if (!(be instanceof PetalApothecary apothecary))
			return 0;
		// don't insert if it's not empty
		if (apothecary.getFluid() != PetalApothecary.State.EMPTY)
			return 0;

		PetalApothecary.State fluidState;

		Fluid fluid = availableFluid.getType().getFluid();
		if (fluid == Fluids.WATER) {
			fluidState = PetalApothecary.State.WATER;
		} else if (fluid == Fluids.LAVA) {
			fluidState = PetalApothecary.State.LAVA;
		} else {
			return 0;
		}

		// don't insert if we have less than a bucket's worth of fluid
		if (availableFluid.getAmount() < FluidConstants.BUCKET) {
			return 0;
		}

		if (!simulate) {
			apothecary.setFluid(fluidState);
		}

		return FluidConstants.BUCKET;
	}

	private boolean enabled() {
		return AllConfigs.server().recipes.allowFillingBySpout.get();
	}

}
