package com.simibubi.create.content.equipment.toolbox;

import com.simibubi.create.infrastructure.fabric.transfer.item.ItemStackHandler;
import com.simibubi.create.infrastructure.fabric.transfer.item.SlotItemHandler;

public class ToolboxSlot extends SlotItemHandler {

	private ToolboxMenu toolboxMenu;
	private boolean isVisible;

	public ToolboxSlot(ToolboxMenu menu, ItemStackHandler itemHandler, int index, int xPosition, int yPosition, boolean isVisible) {
		super(itemHandler, index, xPosition, yPosition);
		this.toolboxMenu = menu;
		this.isVisible = isVisible;
	}

	@Override
	public boolean isActive() {
		return !toolboxMenu.renderPass && super.isActive() && isVisible;
	}

}
