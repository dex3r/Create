package com.simibubi.create;

import net.minecraft.Util;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public class CreateBuildInfo {
	public static final String VERSION = Util.make(() -> {
		ModContainer container = FabricLoader.getInstance().getModContainer(Create.ID).orElseThrow();
		return container.getMetadata().getVersion().getFriendlyString();
	});
}
