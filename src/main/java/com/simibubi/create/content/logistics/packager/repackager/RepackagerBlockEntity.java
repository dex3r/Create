package com.simibubi.create.content.logistics.packager.repackager;

import java.util.List;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.crate.BottomlessItemHandler;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagerItemHandler;
import com.simibubi.create.content.logistics.packager.PackagingRequest;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.callbacks.TransactionCallback;

public class RepackagerBlockEntity extends PackagerBlockEntity {

	public PackageRepackageHelper repackageHelper;

	public RepackagerBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
		repackageHelper = new PackageRepackageHelper();
	}

	public boolean unwrapBox(ItemStack box, TransactionContext ctx) {
		if (animationTicks > 0)
			return false;

		Storage<ItemVariant> targetInv = targetInventory.getInventory();
		if (targetInv == null || targetInv instanceof PackagerItemHandler)
			return false;

		boolean targetIsCreativeCrate = targetInv instanceof BottomlessItemHandler;

		long insertable = StorageUtil.simulateInsert(targetInv, ItemVariant.of(box), box.getCount(), null);
		boolean anySpace = insertable > 0;

		if (!targetIsCreativeCrate && !anySpace)
			return false;

		TransactionCallback.onSuccess(ctx, () -> {
			previouslyUnwrapped = box;
			animationInward = true;
			animationTicks = CYCLE;
			notifyUpdate();
		});

		return true;
	}

	@Override
	public void recheckIfLinksPresent() {
	}

	@Override
	public boolean redstoneModeActive() {
		return true;
	}

	public void attemptToSend(List<PackagingRequest> queuedRequests) {
		if (!heldBox.isEmpty() || animationTicks != 0 || buttonCooldown > 0)
			return;
		if (!queuedExitingPackages.isEmpty())
			return;

		Storage<ItemVariant> targetInv = targetInventory.getInventory();
		if (targetInv == null || targetInv instanceof PackagerItemHandler)
			return;

		attemptToRepackage(targetInv);
		if (heldBox.isEmpty())
			return;

		updateSignAddress();
		if (!signBasedAddress.isBlank())
			PackageItem.addAddress(heldBox, signBasedAddress);
	}

	protected void attemptToRepackage(Storage<ItemVariant> targetInv) {
		repackageHelper.clear();
		int completedOrderId = -1;

		for (StorageView<ItemVariant> view : targetInv.nonEmptyViews()) {
			ItemVariant resource = view.getResource();
			if (!PackageItem.isPackage(resource))
				continue;

			if (!repackageHelper.isFragmented(resource)) {
				try (Transaction t = Transaction.openOuter()) {
					if (view.extract(resource, 1, t) == 1) {
						t.commit();
						heldBox = resource.toStack();
						animationInward = false;
						animationTicks = CYCLE;
						notifyUpdate();
					}
				}
				return;
			}

			ItemStack stack = resource.toStack(TransferUtil.truncateLong(view.getAmount()));
			completedOrderId = repackageHelper.addPackageFragment(stack);
			if (completedOrderId != -1)
				break;
		}

		if (completedOrderId == -1)
			return;

		List<BigItemStack> boxesToExport = repackageHelper.repack(completedOrderId, level.getRandom());

		try (Transaction t = Transaction.openOuter()) {
			for (StorageView<ItemVariant> view : targetInv.nonEmptyViews()) {
				ItemVariant resource = view.getResource();
				if (!PackageItem.isPackage(resource))
					continue;
				if (PackageItem.getOrderId(resource) != completedOrderId)
					continue;
				view.extract(resource, view.getAmount(), t);
			}

			if (boxesToExport.isEmpty()) {
				t.commit();
				return;
			}

			queuedExitingPackages.addAll(boxesToExport);
			t.commit();
		}

		notifyUpdate();
	}

}
