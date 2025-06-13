package com.simibubi.create.foundation.data.recipe;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.api.data.recipe.ItemApplicationRecipeGen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

import io.github.fabricators_of_create.porting_lib.tags.Tags.Items;

/**
 * Create's own Data Generation for Item Application recipes
 * @see ItemApplicationRecipeGen
 */
@SuppressWarnings("unused")
public final class CreateItemApplicationRecipeGen extends ItemApplicationRecipeGen {

	GeneratedRecipe

	BOUND_CARDBOARD_BLOCK = create("bound_cardboard_inworld",
		b -> b.require(AllBlocks.CARDBOARD_BLOCK.asItem())
		.require(Items.STRING)
		.output(AllBlocks.BOUND_CARDBOARD_BLOCK.asStack())),

	ANDESITE = woodCasing("andesite", CreateRecipeProvider.I::andesiteAlloy, CreateRecipeProvider.I::andesiteCasing),
	COPPER = woodCasingTag("copper", CreateRecipeProvider.I::copper, CreateRecipeProvider.I::copperCasing),
	BRASS = woodCasingTag("brass", CreateRecipeProvider.I::brass, CreateRecipeProvider.I::brassCasing),
	RAILWAY = create("railway_casing", b -> b.require(CreateRecipeProvider.I.brassCasing())
		.require(CreateRecipeProvider.I.sturdySheet())
		.output(CreateRecipeProvider.I.railwayCasing()));


	public CreateItemApplicationRecipeGen(FabricDataOutput output) {
		super(output, Create.ID);
	}
}
