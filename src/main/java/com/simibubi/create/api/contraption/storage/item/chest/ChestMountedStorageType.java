package com.simibubi.create.api.contraption.storage.item.chest;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorage;
import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorageType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;

import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

public class ChestMountedStorageType extends SimpleMountedStorageType<ChestMountedStorage> {
	public ChestMountedStorageType() {
		super(ChestMountedStorage.CODEC);
	}

	@Override
	protected SlottedStorage<ItemVariant> getStorage(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
		// the storage provided by FAPI includes both halves, just get 1
		return be instanceof Container container ? InventoryStorage.of(container, null) : null;
	}

	@Override
	protected SimpleMountedStorage createStorage(SlottedStorage<ItemVariant> storage) {
		return new ChestMountedStorage(storage);
	}
}
