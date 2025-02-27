package com.simibubi.create.content.logistics.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllKeys;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.filter.AttributeFilterMenu.WhitelistMode;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.recipe.ItemCopyingRecipe.SupportsItemCopying;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.util.NetworkHooks;

public class FilterItem extends Item implements MenuProvider, SupportsItemCopying {

	private FilterType type;

	private enum FilterType {
		REGULAR, ATTRIBUTE, PACKAGE;
	}

	public static FilterItem regular(Properties properties) {
		return new FilterItem(FilterType.REGULAR, properties);
	}

	public static FilterItem attribute(Properties properties) {
		return new FilterItem(FilterType.ATTRIBUTE, properties);
	}

	public static FilterItem address(Properties properties) {
		return new FilterItem(FilterType.PACKAGE, properties);
	}

	private FilterItem(FilterType type, Properties properties) {
		super(properties);
		this.type = type;
	}

	@Nonnull
	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (context.getPlayer() == null)
			return InteractionResult.PASS;
		return use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		if (AllKeys.shiftDown())
			return;
		List<Component> makeSummary = makeSummary(stack);
		if (makeSummary.isEmpty())
			return;
		tooltip.add(CommonComponents.SPACE);
		tooltip.addAll(makeSummary);
	}

	private List<Component> makeSummary(ItemStack filter) {
		List<Component> list = new ArrayList<>();
		if (!filter.hasTag())
			return list;

		if (type == FilterType.REGULAR) {
			ItemStackHandler filterItems = getFilterItems(filter);
			boolean blacklist = filter.getOrCreateTag()
				.getBoolean("Blacklist");

			list.add((blacklist ? CreateLang.translateDirect("gui.filter.deny_list")
				: CreateLang.translateDirect("gui.filter.allow_list")).withStyle(ChatFormatting.GOLD));
			int count = 0;
			for (int i = 0; i < filterItems.getSlotCount(); i++) {
				if (count > 3) {
					list.add(Component.literal("- ...")
						.withStyle(ChatFormatting.DARK_GRAY));
					break;
				}

				ItemStack filterStack = filterItems.getStackInSlot(i);
				if (filterStack.isEmpty())
					continue;
				list.add(Component.literal("- ")
					.append(filterStack.getHoverName())
					.withStyle(ChatFormatting.GRAY));
				count++;
			}

			if (count == 0)
				return Collections.emptyList();
		}

		if (type == FilterType.ATTRIBUTE) {
			WhitelistMode whitelistMode = WhitelistMode.values()[filter.getOrCreateTag()
				.getInt("WhitelistMode")];
			list.add((whitelistMode == WhitelistMode.WHITELIST_CONJ
				? CreateLang.translateDirect("gui.attribute_filter.allow_list_conjunctive")
				: whitelistMode == WhitelistMode.WHITELIST_DISJ
				? CreateLang.translateDirect("gui.attribute_filter.allow_list_disjunctive")
				: CreateLang.translateDirect("gui.attribute_filter.deny_list")).withStyle(ChatFormatting.GOLD));

			int count = 0;
			ListTag attributes = filter.getOrCreateTag()
				.getList("MatchedAttributes", Tag.TAG_COMPOUND);
			for (Tag inbt : attributes) {
				CompoundTag compound = (CompoundTag) inbt;
				ItemAttribute attribute = ItemAttribute.loadStatic(compound);
				if (attribute == null)
					continue;
				boolean inverted = compound.getBoolean("Inverted");
				if (count > 3) {
					list.add(Component.literal("- ...")
						.withStyle(ChatFormatting.DARK_GRAY));
					break;
				}
				list.add(Component.literal("- ")
					.append(attribute.format(inverted)));
				count++;
			}

			if (count == 0)
				return Collections.emptyList();
		}

		if (type == FilterType.PACKAGE) {
			String address = filter.getOrCreateTag()
				.getString("Address");
			if (!address.isBlank())
				list.add(CreateLang.text("-> ")
					.style(ChatFormatting.GRAY)
					.add(CreateLang.text(address)
						.style(ChatFormatting.GOLD))
					.component());
		}

		return list;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack heldItem = player.getItemInHand(hand);

		if (!player.isShiftKeyDown() && hand == InteractionHand.MAIN_HAND) {
			if (!world.isClientSide && player instanceof ServerPlayer serverPlayer)
				NetworkHooks.openScreen(serverPlayer, this, buf -> {
					buf.writeItem(heldItem);
				});
			return InteractionResultHolder.success(heldItem);
		}
		return InteractionResultHolder.pass(heldItem);
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		ItemStack heldItem = player.getMainHandItem();
		if (type == FilterType.REGULAR)
			return FilterMenu.create(id, inv, heldItem);
		if (type == FilterType.ATTRIBUTE)
			return AttributeFilterMenu.create(id, inv, heldItem);
		if (type == FilterType.PACKAGE)
			return PackageFilterMenu.create(id, inv, heldItem);
		return null;
	}

	@Override
	public Component getDisplayName() {
		return getDescription();
	}

	public static ItemStackHandler getFilterItems(ItemStack stack) {
		ItemStackHandler newInv = new ItemStackHandler(18);
		if (AllItems.FILTER.get() != stack.getItem())
			throw new IllegalArgumentException("Cannot get filter items from non-filter: " + stack);
		if (!stack.hasTag())
			return newInv;
		CompoundTag invNBT = stack.getOrCreateTagElement("Items");
		if (!invNBT.isEmpty())
			newInv.deserializeNBT(invNBT);
		return newInv;
	}

	public static boolean testDirect(ItemStack filter, ItemStack stack, boolean matchNBT) {
		if (matchNBT) {
			if (PackageItem.isPackage(filter) && PackageItem.isPackage(stack))
				return doPackagesHaveSameData(filter, stack);

			return ItemHandlerHelper.canItemStacksStack(filter, stack);
		}

		if (PackageItem.isPackage(filter) && PackageItem.isPackage(stack))
			return true;

		return ItemHelper.sameItem(filter, stack);
	}

	public static boolean doPackagesHaveSameData(@NotNull ItemStack a, @NotNull ItemStack b) {
		if (a.isEmpty() || a.hasTag() != b.hasTag())
			return false;
		if (!a.hasTag())
			return true;
		if (!a.areCapsCompatible(b))
			return false;
		for (String key : a.getTag()
			.getAllKeys()) {
			if (key.equals("Fragment"))
				continue;
			if (!Objects.equals(a.getTag()
					.get(key),
				b.getTag()
					.get(key)))
				return false;
		}
		return true;
	}

}
