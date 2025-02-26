package com.simibubi.create.foundation.mixin.compat.ftbchunks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import dev.ftb.mods.ftbchunks.client.gui.RegionMapPanel;

@Mixin(RegionMapPanel.class)
public interface RegionMapPanelAccessor {
	@Accessor
	int getRegionMinX();

	@Accessor
	int getRegionMinZ();
}
