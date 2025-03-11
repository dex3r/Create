package com.simibubi.create.infrastructure.fabric.transfer.item;

import net.minecraft.world.inventory.Slot;

public class SlotItemHandler extends Slot {
	public SlotItemHandler(SlottedStackStorage storage, int slot, int x, int y) {
		super(new StorageWrapperContainer(storage), slot, x, y);
	}
}
