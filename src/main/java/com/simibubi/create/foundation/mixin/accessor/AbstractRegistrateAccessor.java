package com.simibubi.create.foundation.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.RegistrateDataProvider;

@Mixin(AbstractRegistrate.class)
public interface AbstractRegistrateAccessor {
	@Accessor("provider")
	void create$setProvider(RegistrateDataProvider provider);
}
