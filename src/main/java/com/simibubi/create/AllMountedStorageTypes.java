package com.simibubi.create;

import static com.simibubi.create.Create.REGISTRATE;

import java.util.function.Supplier;

import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.contraption.storage.item.chest.ChestMountedStorageType;
import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorageType;
import com.simibubi.create.content.contraptions.behaviour.dispenser.storage.DispenserMountedStorageType;
import com.simibubi.create.content.equipment.toolbox.ToolboxMountedStorageType;
import com.simibubi.create.content.fluids.tank.storage.FluidTankMountedStorageType;
import com.simibubi.create.content.fluids.tank.storage.creative.CreativeFluidTankMountedStorageType;
import com.simibubi.create.content.logistics.crate.CreativeCrateMountedStorageType;
import com.simibubi.create.content.logistics.depot.storage.DepotMountedStorageType;
import com.simibubi.create.content.logistics.vault.ItemVaultMountedStorageType;
import com.tterrag.registrate.util.entry.RegistryEntry;

import net.minecraft.world.level.block.Blocks;

public class AllMountedStorageTypes {
	// fabric: no fallback, there is no common simple implementation

	// registrations for these are handled by the blocks, not the types
	public static final RegistryEntry<CreativeCrateMountedStorageType> CREATIVE_CRATE = simpleItem("creative_crate", CreativeCrateMountedStorageType::new);
	public static final RegistryEntry<ItemVaultMountedStorageType> VAULT = simpleItem("vault", ItemVaultMountedStorageType::new);
	public static final RegistryEntry<ToolboxMountedStorageType> TOOLBOX = simpleItem("toolbox", ToolboxMountedStorageType::new);
	public static final RegistryEntry<DepotMountedStorageType> DEPOT = simpleItem("depot", DepotMountedStorageType::new);
	public static final RegistryEntry<FluidTankMountedStorageType> FLUID_TANK = simpleFluid("fluid_tank", FluidTankMountedStorageType::new);
	public static final RegistryEntry<CreativeFluidTankMountedStorageType> CREATIVE_FLUID_TANK = simpleFluid("creative_fluid_tank", CreativeFluidTankMountedStorageType::new);

	// these are for external blocks, register associations here
	public static final RegistryEntry<SimpleMountedStorageType.Impl> SIMPLE = REGISTRATE.mountedItemStorage("simple", SimpleMountedStorageType.Impl::new)
		.associateBlockTag(AllTags.AllBlockTags.SIMPLE_MOUNTED_STORAGE.tag)
		.register();
	public static final RegistryEntry<ChestMountedStorageType> CHEST = REGISTRATE.mountedItemStorage("chest", ChestMountedStorageType::new)
		.associateBlockTag(AllTags.AllBlockTags.CHEST_MOUNTED_STORAGE.tag)
		.register();
	public static final RegistryEntry<DispenserMountedStorageType> DISPENSER = REGISTRATE.mountedItemStorage("dispenser", DispenserMountedStorageType::new)
		.associate(Blocks.DISPENSER)
		.associate(Blocks.DROPPER)
		.register();

	private static <T extends MountedItemStorageType<?>> RegistryEntry<T> simpleItem(String name, Supplier<T> supplier) {
		return REGISTRATE.mountedItemStorage(name, supplier).register();
	}

	private static <T extends MountedFluidStorageType<?>> RegistryEntry<T> simpleFluid(String name, Supplier<T> supplier) {
		return REGISTRATE.mountedFluidStorage(name, supplier).register();
	}

	public static void register() {
	}
}
