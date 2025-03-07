package com.simibubi.create.foundation.block;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;

import com.google.common.collect.HashBiMap;

import net.minecraft.world.level.block.Block;

@ApiStatus.Internal
public class CopperRegistries {
	private static final Map<Supplier<Block>, Supplier<Block>> WEATHERING = HashBiMap.create();
	private static final Map<Supplier<Block>, Supplier<Block>> WAXABLE = HashBiMap.create();

	public static Map<Supplier<Block>, Supplier<Block>> getWeatheringView() {
		return Collections.unmodifiableMap(WEATHERING);
	}

	public static Map<Supplier<Block>, Supplier<Block>> getWaxableView() {
		return Collections.unmodifiableMap(WAXABLE);
	}

	public static synchronized void addWeathering(Supplier<Block> original, Supplier<Block> weathered) {
		WEATHERING.put(original, weathered);
	}

	public static synchronized void addWaxable(Supplier<Block> original, Supplier<Block> waxed) {
		WAXABLE.put(original, waxed);
	}
}
