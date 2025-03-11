package com.simibubi.create.content.processing.basin;

import com.simibubi.create.foundation.item.SmartInventory;

import com.simibubi.create.infrastructure.fabric.item.ItemUtils;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class BasinInventory extends SmartInventory {

	private BasinBlockEntity blockEntity;

	public boolean packagerMode;

	public BasinInventory(int slots, BasinBlockEntity be) {
		super(slots, be, 64, true);
		this.blockEntity = be;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notBlankNotNegative(resource, maxAmount);
		if (!insertionAllowed)
			return 0;

		if (packagerMode) // Unique stack insertion only matters for belt setups
			return super.insert(resource, maxAmount, transaction);

		// Only insert if no other slot already has a stack of this item
		try (Transaction test = transaction.openNested()) {
			long contained = this.extract(resource, Long.MAX_VALUE, test);
			if (contained != 0) {
				// already have this item. can we stack?
				long maxStackSize = Math.min(stackSize, ItemUtils.getMaxStackSize(resource));
				long space = Math.max(0, maxStackSize - contained);
				if (space <= 0) {
					// nope.
					return 0;
				} else {
					// yes!
					maxAmount = Math.min(space, maxAmount);
				}
			}
		}
		return super.insert(resource, maxAmount, transaction);
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		long extractedAmount = super.extract(resource, maxAmount, transaction);
		if (extractedAmount != 0)
			blockEntity.notifyChangeOfContents();
		return extractedAmount;
	}

}
