package com.simibubi.create.content.redstone.displayLink.source;

import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorPackage;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.content.redstone.smartObserver.SmartObserverBlock;
import com.simibubi.create.content.redstone.smartObserver.SmartObserverBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;

public class PackageAddressDisplaySource extends SingleLineDisplaySource {

	@Override
	protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
		BlockEntity sourceBE = context.getSourceBlockEntity();
		if (!(sourceBE instanceof SmartObserverBlockEntity cobe))
			return EMPTY_LINE;

		InvManipulationBehaviour invManipulationBehaviour = cobe.getBehaviour(InvManipulationBehaviour.TYPE);
		FilteringBehaviour filteringBehaviour = cobe.getBehaviour(FilteringBehaviour.TYPE);
		Storage<ItemVariant> handler = invManipulationBehaviour.getInventory();

		if (handler == null) {
			BlockPos targetPos = cobe.getBlockPos().relative(SmartObserverBlock.getTargetDirection(cobe.getBlockState()));

			if (context.level().getBlockEntity(targetPos) instanceof ChainConveyorBlockEntity ccbe)
				for (ChainConveyorPackage box : ccbe.getLoopingPackages())
					if (filteringBehaviour.test(box.item))
						return Component.literal(PackageItem.getAddress(box.item));

			return EMPTY_LINE;
		}

		for (StorageView<ItemVariant> view : handler.nonEmptyViews()) {
			ItemVariant resource = view.getResource();
			if (PackageItem.isPackage(resource) && filteringBehaviour.test(resource))
				return Component.literal(PackageItem.getAddress(resource));
		}

		return EMPTY_LINE;
	}

	@Override
	protected String getTranslationKey() {
		return "read_package_address";
	}

	@Override
	protected boolean allowsLabeling(DisplayLinkContext context) {
		return true;
	}

	@Override
	protected String getFlapDisplayLayoutName(DisplayLinkContext context) {
		return "Default";
	}

}
