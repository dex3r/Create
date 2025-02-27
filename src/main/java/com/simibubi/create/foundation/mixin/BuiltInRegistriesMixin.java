package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.simibubi.create.api.registry.CreateBuiltInRegistries;

import net.minecraft.core.registries.BuiltInRegistries;

@Mixin(BuiltInRegistries.class)
public class BuiltInRegistriesMixin {
	static {
		CreateBuiltInRegistries.init();
	}
}
