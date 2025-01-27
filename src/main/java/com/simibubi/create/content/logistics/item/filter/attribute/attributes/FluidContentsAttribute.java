package com.simibubi.create.content.logistics.item.filter.attribute.attributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.logistics.item.filter.attribute.AllItemAttributeTypes;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

public class FluidContentsAttribute implements ItemAttribute {
	private @Nullable Fluid fluid;

	public FluidContentsAttribute(@Nullable Fluid fluid) {
		this.fluid = fluid;
	}

	private static List<Fluid> extractFluids(ItemStack stack) {
		Storage<FluidVariant> storage = ContainerItemContext.withConstant(stack).find(FluidStorage.ITEM);
		if (storage == null)
			return List.of();

		List<Fluid> fluids = new ArrayList<>();
		for (StorageView<FluidVariant> view : storage.nonEmptyViews()) {
			fluids.add(view.getResource().getFluid());
		}
		return fluids;
	}

	@Override
	public boolean appliesTo(ItemStack itemStack, Level level) {
		return extractFluids(itemStack).contains(fluid);
	}

	@Override
	public String getTranslationKey() {
		return "has_fluid";
	}

	@Override
	public Object[] getTranslationParameters() {
		String parameter = "";
		if (fluid != null)
			parameter = fluid.getFluidType().getDescription().getString();
		return new Object[]{parameter};
	}

	@Override
	public ItemAttributeType getType() {
		return AllItemAttributeTypes.HAS_FLUID.get();
	}

	@Override
	public void save(CompoundTag nbt) {
		if (fluid == null)
			return;
		Optional<ResourceLocation> id = BuiltInRegistries.FLUID.getResourceKey(fluid).map(ResourceKey::location);
		if (id.isEmpty())
			return;
		nbt.putString("id", id.get().toString());
	}

	@Override
	public void load(CompoundTag nbt) {
		if (nbt.contains("id")) {
			fluid = BuiltInRegistries.FLUID.get(ResourceLocation.tryParse(nbt.getString("id")));
		}
	}

	public static class Type implements ItemAttributeType {
		@Override
		public @NotNull ItemAttribute createAttribute() {
			return new FluidContentsAttribute(null);
		}

		@Override
		public List<ItemAttribute> getAllAttributes(ItemStack stack, Level level) {
			List<ItemAttribute> list = new ArrayList<>();

			for (Fluid fluid : extractFluids(stack)) {
				list.add(new FluidContentsAttribute(fluid));
			}

			return list;
		}
	}
}
