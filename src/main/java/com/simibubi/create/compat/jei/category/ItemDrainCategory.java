package com.simibubi.create.compat.jei.category;

import java.util.function.Consumer;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.Create;
import com.simibubi.create.compat.jei.category.animations.AnimatedItemDrain;
import com.simibubi.create.content.fluids.potion.PotionFluidHandler;
import com.simibubi.create.content.fluids.transfer.EmptyingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.item.ItemHelper;

import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.fabric.constants.FabricTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IIngredientManager;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.fabricators_of_create.porting_lib.transfer.MutableContainerItemContext;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;

@ParametersAreNonnullByDefault
public class ItemDrainCategory extends CreateRecipeCategory<EmptyingRecipe> {

	private final AnimatedItemDrain drain = new AnimatedItemDrain();

	public ItemDrainCategory(Info<EmptyingRecipe> info) {
		super(info);
	}

	public static void consumeRecipes(Consumer<EmptyingRecipe> consumer, IIngredientManager ingredientManager) {
		ObjectOpenCustomHashSet<ItemStack> emptiedItems = new ObjectOpenCustomHashSet<>(ItemStackLinkedSet.TYPE_AND_TAG);
		for (ItemStack stack : ingredientManager.getAllIngredients(VanillaTypes.ITEM_STACK)) {
			if (PotionFluidHandler.isPotionItem(stack)) {
				FluidStack fluidFromPotionItem = PotionFluidHandler.getFluidFromPotionItem(stack);
				Ingredient potion = Ingredient.of(stack);
				consumer.accept(new ProcessingRecipeBuilder<>(EmptyingRecipe::new, Create.asResource("potions"))
					.withItemIngredients(potion)
					.withFluidOutputs(fluidFromPotionItem)
					.withSingleItemOutput(new ItemStack(Items.GLASS_BOTTLE))
					.build());
				continue;
			}

			ContainerItemContext testCtx = ContainerItemContext.withConstant(stack);
			Storage<FluidVariant> testStorage = testCtx.find(FluidStorage.ITEM);
			if (testStorage == null)
				continue;

			ItemStack copy = stack.copy();
			MutableContainerItemContext ctx = new MutableContainerItemContext(copy);
			Storage<FluidVariant> storage = ctx.find(FluidStorage.ITEM);
			FluidStack extracted = TransferUtil.extractAnyFluid(storage, FluidConstants.BUCKET);
			ItemVariant result = ctx.getItemVariant();
			if (extracted.isEmpty())
				continue;
			if (result.isBlank())
				continue;

			// There can be a lot of duplicate empty tanks (e.g. from emptying tanks with different fluids). Merge
			// them to reduce memory usage. If the item is exactly the same as the input, just use the input stack
			// instead of the copy.
			result = ItemHelper.sameItem(stack, result) ? stack : emptiedItems.addOrGet(result);

			Ingredient ingredient = Ingredient.of(stack);
			ResourceLocation itemName = CatnipServices.REGISTRIES.getKeyOrThrow(stack.getItem());
			ResourceLocation fluidName = CatnipServices.REGISTRIES.getKeyOrThrow(extracted.getFluid());

			consumer.accept(new ProcessingRecipeBuilder<>(EmptyingRecipe::new,
				Create.asResource("empty_" + itemName.getNamespace() + "_" + itemName.getPath() + "_of_"
					+ fluidName.getNamespace() + "_" + fluidName.getPath())).withItemIngredients(ingredient)
						.withFluidOutputs(extracted)
						.withSingleItemOutput(result.toStack(ItemHelper.truncateLong(ctx.getAmount())))
						.build());
		}
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, EmptyingRecipe recipe, IFocusGroup focuses) {
		builder
				.addSlot(RecipeIngredientRole.INPUT, 27, 8)
				.setBackground(getRenderedSlot(), -1, -1)
				.addIngredients(recipe.getIngredients().get(0));
		builder
				.addSlot(RecipeIngredientRole.OUTPUT, 132, 8)
				.setBackground(getRenderedSlot(), -1, -1)
				.addIngredient(FabricTypes.FLUID_STACK, toJei(withImprovedVisibility(recipe.getResultingFluid())))
				.addRichTooltipCallback(addFluidTooltip(recipe.getResultingFluid().getAmount()));
		builder
				.addSlot(RecipeIngredientRole.OUTPUT, 132, 27)
				.setBackground(getRenderedSlot(), -1, -1)
				.addItemStack(getResultItem(recipe));
	}

	@Override
	public void draw(EmptyingRecipe recipe, IRecipeSlotsView iRecipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
		AllGuiTextures.JEI_SHADOW.render(graphics, 62, 37);
		AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 73, 4);
		drain.withFluid(recipe.getResultingFluid())
			.draw(graphics, getBackground().getWidth() / 2 - 13, 40);
	}

}
