package com.simibubi.create.foundation.utility;

import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;

import java.util.List;

/**
 * Specialized combined inventory wrapper with faster slot -> inv lookup
 * for the case when all inventories are the same size.
 *
 * <p>For context, CombinedInvWrapper implements this lookup by doing a linear scan of base indices per-inventory.
 * We could optimize this by using a binary search, however for vaults we control all the inventories going into
 * this and know that they all have the same number of slots. Just dividing by the number of slots per inventory
 * is sufficient to get the inventory index.
 *
 * <p>Throw in some sanity checks and fallbacks so this isn't obscenely fragile.
 */
public class SameSizeCombinedInvWrapper<T, S extends Storage<T>> extends CombinedStorage<T, S> {

	private final int numSlotsPerInv;
	private final int numCombinedSlots;

	private SameSizeCombinedInvWrapper(int numSlotsPerInv, List<S> parts) {
		super(parts);

		this.numSlotsPerInv = numSlotsPerInv;
		this.numCombinedSlots = numSlotsPerInv * parts.size();
	}

	/**
	 * Create a SameSizeCombinedInvWrapper if all item handlers actually have the same size.
	 * Otherwise, falls back to the parent class.
	 */
	public static <T, S extends Storage<T>> CombinedStorage<T, S> create(List<S> parts) {
		if (parts.isEmpty()) {
			// No need to subclass here.
			// Early out because we need to validate that all slots have the same length.
			return new CombinedStorage<>(parts);
		}

		// If any inventories have different slot counts, fall back to the default impl.
		int firstInvNumSlots = parts.get(0).getSlots();
		for (int i = 1; i < itemHandler.length; i++) {
			if (firstInvNumSlots != itemHandler[i].getSlots()) {
				return new CombinedInvWrapper(itemHandler);
			}
		}

		return new SameSizeCombinedInvWrapper(firstInvNumSlots, itemHandler);
	}

	@Override
	protected int getIndexForSlot(int slot) {
		// The parent class agrees than -1 means invalid input.
		if (slot < 0 || slot >= numCombinedSlots) {
			return -1;
		}

		// Floor div go brr.
		return slot / numSlotsPerInv;
	}
}
