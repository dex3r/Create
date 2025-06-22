package com.simibubi.create.foundation.utility;

import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;

// TODO - Fix
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
public final class SameSizeCombinedInvWrapper<T, S extends Storage<T>> extends CombinedStorage<T, S> {

	private final long numSlotsPerInv;
	private final long numCombinedSlots;

	private SameSizeCombinedInvWrapper(long numSlotsPerInv, List<S> parts) {
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
		Long firstInvNumSlots = null;
		for (StorageView<T> storageView : parts.get(0)) {
			if (firstInvNumSlots == null)
				firstInvNumSlots = storageView.getCapacity();

			if (firstInvNumSlots != storageView.getCapacity()) {
				return new CombinedStorage<>(parts);
			}
		}

		if (firstInvNumSlots == null)
			firstInvNumSlots = 0L;

		return new SameSizeCombinedInvWrapper<>(firstInvNumSlots, parts);
	}
}
