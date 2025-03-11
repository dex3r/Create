package com.simibubi.create.api.contraption.storage.item.menu;

import java.util.function.Consumer;
import java.util.function.Predicate;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

import com.simibubi.create.infrastructure.fabric.transfer.item.SlottedStackStorage;

public class StorageInteractionWrapper implements Container {
	private final SlottedStackStorage storage;
	private final Predicate<Player> stillValid;
	private final Consumer<Player> onClose;

	public StorageInteractionWrapper(SlottedStackStorage storage, Predicate<Player> stillValid, Consumer<Player> onClose) {
		this.storage = storage;
		this.stillValid = stillValid;
		this.onClose = onClose;
	}

	@Override
	public boolean stillValid(Player player) {
		return this.stillValid.test(player);
	}

	@Override
	public void stopOpen(Player player) {
		this.onClose.accept(player);
	}

	@Override
	public int getContainerSize() {
		return this.storage.getSlotCount();
	}

	@Override
	public boolean isEmpty() {
		return this.storage.nonEmptyIterator().hasNext();
	}

	@Override
	public ItemStack getItem(int slot) {
		return this.storage.getStackInSlot(slot);
	}

	@Override
	public ItemStack removeItem(int index, int count) {
		if (index >= 0 && index < this.storage.getSlotCount()) {
			ItemStack current = this.storage.getStackInSlot(index);
			if (current.isEmpty())
				return ItemStack.EMPTY;
			current = current.copy();
			ItemStack extracted = current.split(count);
			this.storage.setStackInSlot(index, current);
			return extracted;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItemNoUpdate(int index) {
		return removeItem(index, Integer.MAX_VALUE);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		this.storage.setStackInSlot(slot, stack);
	}

	@Override
	public void setChanged() {
	}

	@Override
	public boolean canPlaceItem(int index, ItemStack stack) {
		return this.storage.isItemValid(index, ItemVariant.of(stack), stack.getCount());
	}

	@Override
	public void clearContent() {
		for (int i = 0; i < this.storage.getSlotCount(); i++) {
			this.storage.setStackInSlot(i, ItemStack.EMPTY);
		}
	}
}
