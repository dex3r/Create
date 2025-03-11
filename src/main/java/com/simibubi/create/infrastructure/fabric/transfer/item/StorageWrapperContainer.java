package com.simibubi.create.infrastructure.fabric.transfer.item;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record StorageWrapperContainer(SlottedStackStorage storage) implements Container {
	@Override
	public int getContainerSize() {
		return this.storage.getSlotCount();
	}

	@Override
	public boolean isEmpty() {
		return !this.storage.nonEmptyIterator().hasNext();
	}

	@Override
	public ItemStack getItem(int slot) {
		return this.storage.getStackInSlot(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		ItemStack stack = this.storage.getStackInSlot(slot).copy();
		ItemStack split = stack.split(amount);
		this.storage.setStackInSlot(slot, stack);
		return split;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		ItemStack stack = this.storage.getStackInSlot(slot);
		this.storage.setStackInSlot(slot, ItemStack.EMPTY);
		return stack;
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		this.storage.setStackInSlot(slot, stack.copy());
	}

	@Override
	public void setChanged() {
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return this.storage.isItemValid(slot, stack);
	}

	@Override
	public void clearContent() {
		for (int i = 0; i < this.storage.getSlotCount(); i++) {
			this.storage.setStackInSlot(i, ItemStack.EMPTY);
		}
	}
}
