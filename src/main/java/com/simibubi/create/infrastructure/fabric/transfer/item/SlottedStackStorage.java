package com.simibubi.create.infrastructure.fabric.transfer.item;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;

import net.minecraft.world.item.ItemStack;

public interface SlottedStackStorage extends SlottedStorage<ItemVariant> {
	ItemStack getStackInSlot(int slot);

	void setStackInSlot(int slot, ItemStack stack);

	int getSlotLimit(int slot);

	boolean isItemValid(int slot, ItemStack stack);
}
