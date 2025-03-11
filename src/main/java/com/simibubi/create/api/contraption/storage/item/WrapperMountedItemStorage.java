package com.simibubi.create.api.contraption.storage.item;

import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.UnmodifiableView;

import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import com.simibubi.create.infrastructure.fabric.transfer.TransferUtil;
import com.simibubi.create.infrastructure.fabric.transfer.item.ItemStackHandler;
import com.simibubi.create.infrastructure.fabric.transfer.item.SlottedStackStorage;

/**
 * Partial implementation of a MountedItemStorage that wraps an item handler.
 */
public abstract class WrapperMountedItemStorage<T extends SlottedStackStorage> extends MountedItemStorage {
	protected final T wrapped;

	protected WrapperMountedItemStorage(MountedItemStorageType<?> type, T wrapped) {
		super(type);
		this.wrapped = wrapped;
	}

	@Override
	public boolean supportsInsertion() {
		return this.wrapped.supportsInsertion();
	}

	@Override
	public long insert(ItemVariant variant, long l, TransactionContext transactionContext) {
		return this.wrapped.insert(variant, l, transactionContext);
	}

	@Override
	public boolean supportsExtraction() {
		return this.wrapped.supportsExtraction();
	}

	@Override
	public long extract(ItemVariant variant, long l, TransactionContext transactionContext) {
		return this.wrapped.extract(variant, l, transactionContext);
	}

	@Override
	public Iterator<StorageView<ItemVariant>> iterator() {
		return this.wrapped.iterator();
	}

	@Override
	public Iterator<StorageView<ItemVariant>> nonEmptyIterator() {
		return this.wrapped.nonEmptyIterator();
	}

	@Override
	public Iterable<StorageView<ItemVariant>> nonEmptyViews() {
		return this.wrapped.nonEmptyViews();
	}

	@Override
	public long getVersion() {
		return this.wrapped.getVersion();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return this.wrapped.getStackInSlot(slot);
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		this.wrapped.setStackInSlot(slot, stack);
	}

	@Override
	public int getSlotLimit(int slot) {
		return this.wrapped.getSlotLimit(slot);
	}

	@Override
	public boolean isItemValid(int slot, ItemVariant resource, int count) {
		return this.wrapped.isItemValid(slot, resource, count);
	}

	@Override
	public long insertSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return this.wrapped.insertSlot(slot, resource, maxAmount, transaction);
	}

	@Override
	public long extractSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return this.wrapped.extractSlot(slot, resource, maxAmount, transaction);
	}

	@Override
	public int getSlotCount() {
		return this.wrapped.getSlotCount();
	}

	@Override
	public SingleSlotStorage<ItemVariant> getSlot(int i) {
		return this.wrapped.getSlot(i);
	}

	@Override
	@UnmodifiableView
	public List<SingleSlotStorage<ItemVariant>> getSlots() {
		return this.wrapped.getSlots();
	}

	public static ItemStackHandler copyToItemStackHandler(SlottedStorage<ItemVariant> storage) {
		ItemStack[] array = new ItemStack[storage.getSlotCount()];
		for (int i = 0; i < array.length; i++) {
			SingleSlotStorage<ItemVariant> slot = storage.getSlot(i);
			if (slot.isResourceBlank()) {
				array[i] = ItemStack.EMPTY;
			} else {
				int amount = TransferUtil.truncateLong(slot.getAmount());
				ItemStack stack = slot.getResource().toStack(amount);
				array[i] = stack;
			}
		}
		return new ItemStackHandler(array);
	}
}
