package com.simibubi.create.content.logistics.packager;

import java.util.Optional;

import com.simibubi.create.content.logistics.box.PackageItem;

import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

import io.github.fabricators_of_create.porting_lib.transfer.callbacks.TransactionCallback;

public class PackagerItemHandler extends SnapshotParticipant<Optional<ItemStack>> implements SingleSlotStorage<ItemVariant> {

	private final PackagerBlockEntity blockEntity;

	private Optional<ItemStack> newHeldBox = Optional.empty();

	public PackagerItemHandler(PackagerBlockEntity blockEntity) {
		this.blockEntity = blockEntity;
	}

	private ItemStack getHeldBox() {
		return this.newHeldBox.orElse(this.blockEntity.heldBox);
	}

	private void setHeldBox(ItemStack stack) {
		this.newHeldBox = Optional.of(stack);
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notBlankNotNegative(resource, maxAmount);
		ItemStack box = this.getHeldBox();
		if (!box.isEmpty() || !blockEntity.queuedExitingPackages.isEmpty())
			return 0;
		if (!PackageItem.isPackage(resource))
			return 0;
		ItemStack stack = resource.toStack(1);
		if (blockEntity.unwrapBox(stack, transaction)) {
			TransactionCallback.onSuccess(transaction, blockEntity::scheduleStockCheck);
			return 1;
		} else {
			return 0;
		}
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notBlankNotNegative(resource, maxAmount);
		if (blockEntity.animationTicks != 0)
			return 0;
		ItemStack box = this.getHeldBox();
		if (!resource.matches(box))
			return 0;

		int toExtract = (int) Math.min(maxAmount, box.getCount());
		int newSize = box.getCount() - toExtract;

		this.updateSnapshots(transaction);
		this.setHeldBox(box.copyWithCount(newSize));

		return box.getCount();
	}

	@Override
	public boolean isResourceBlank() {
		return blockEntity.heldBox.isEmpty();
	}

	@Override
	public ItemVariant getResource() {
		return ItemVariant.of(blockEntity.heldBox);
	}

	@Override
	public long getAmount() {
		return blockEntity.heldBox.getCount();
	}

	@Override
	public long getCapacity() {
		return 1;
	}

	@Override
	protected void readSnapshot(Optional<ItemStack> snapshot) {
		this.newHeldBox = snapshot;
	}

	@Override
	protected Optional<ItemStack> createSnapshot() {
		return this.newHeldBox;
	}

	@Override
	protected void onFinalCommit() {
		this.newHeldBox.ifPresent(stack -> {
			this.blockEntity.heldBox = stack;
			this.blockEntity.notifyUpdate();
		});

		this.newHeldBox = Optional.empty();
	}
}
