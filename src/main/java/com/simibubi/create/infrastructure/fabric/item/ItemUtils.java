package com.simibubi.create.infrastructure.fabric.item;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

public class ItemUtils {
	public static boolean isComponentsPatchEmpty(ItemStack stack) {
		return stack.getComponentsPatch().isEmpty();
	}

	public static int getMaxStackSize(ItemVariant variant) {
		return variant.getComponentMap().getOrDefault(DataComponents.MAX_STACK_SIZE, 1);
	}
}
