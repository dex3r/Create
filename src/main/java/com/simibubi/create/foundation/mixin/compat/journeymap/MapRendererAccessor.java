package com.simibubi.create.foundation.mixin.compat.journeymap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import journeymap.client.render.map.MapRenderer;

@Mixin(MapRenderer.class)
public interface MapRendererAccessor {
	@Accessor("centerBlockX")
	double create$getCenterBlockX();

	@Accessor("centerBlockZ")
	double create$getCenterBlockZ();
}
