package com.simibubi.create.infrastructure.worldgen;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.simibubi.create.Create;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import io.github.fabricators_of_create.porting_lib.util.LazyRegistrar;

public class AllPlacementModifiers {
	private static final LazyRegistrar<PlacementModifierType<?>> REGISTER = LazyRegistrar.create(Registries.PLACEMENT_MODIFIER_TYPE, Create.ID);

	public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<ConfigPlacementFilter>> CONFIG_FILTER = REGISTER.register("config_filter", () -> () -> ConfigPlacementFilter.CODEC);

	@Internal
	public static void register() {
		REGISTER.register();
	}
}
