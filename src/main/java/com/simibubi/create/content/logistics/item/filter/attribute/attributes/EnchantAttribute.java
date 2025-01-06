package com.simibubi.create.content.logistics.item.filter.attribute.attributes;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.logistics.item.filter.attribute.AllItemAttributeTypes;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;

import net.createmod.catnip.utility.NBTHelper;
import net.createmod.catnip.utility.lang.Components;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

public class EnchantAttribute implements ItemAttribute {
	private @Nullable Enchantment enchantment;

	public EnchantAttribute(@Nullable Enchantment enchantment) {
		this.enchantment = enchantment;
	}

	@Override
	public boolean appliesTo(ItemStack itemStack, Level level) {
		return EnchantmentHelper.getEnchantments(itemStack).containsKey(enchantment);
	}

	@Override
	public String getTranslationKey() {
		return "has_enchant";
	}

	@Override
	public Object[] getTranslationParameters() {
		String parameter = "";
		if (enchantment != null)
			parameter = Components.translatable(enchantment.getDescriptionId()).getString();
		return new Object[]{parameter};
	}

	@Override
	public ItemAttributeType getType() {
		return AllItemAttributeTypes.HAS_ENCHANT.get();
	}

	@Override
	public void save(CompoundTag nbt) {
		if (enchantment == null)
			return;
		ResourceLocation id = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
		if (id == null)
			return;
		NBTHelper.writeResourceLocation(nbt, "enchantId", id);
	}

	@Override
	public void load(CompoundTag nbt) {
		if (nbt.contains("enchantId")) {
			enchantment = BuiltInRegistries.ENCHANTMENT.get(NBTHelper.readResourceLocation(nbt, "enchantId"));
		}
	}

	public static class Type implements ItemAttributeType {
		@Override
		public @NotNull ItemAttribute createAttribute() {
			return new EnchantAttribute(null);
		}

		@Override
		public List<ItemAttribute> getAllAttributes(ItemStack stack, Level level) {
			List<ItemAttribute> list = new ArrayList<>();

			for (Enchantment enchantment : EnchantmentHelper.getEnchantments(stack).keySet()) {
				list.add(new EnchantAttribute(enchantment));
			}

			return list;
		}
	}
}
