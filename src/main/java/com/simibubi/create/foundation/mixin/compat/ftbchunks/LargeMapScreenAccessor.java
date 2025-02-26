package com.simibubi.create.foundation.mixin.compat.ftbchunks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import dev.ftb.mods.ftbchunks.client.gui.LargeMapScreen;
import dev.ftb.mods.ftbchunks.client.gui.RegionMapPanel;

@Mixin(LargeMapScreen.class)
public interface LargeMapScreenAccessor {
	@Accessor
	RegionMapPanel getRegionPanel();
}
