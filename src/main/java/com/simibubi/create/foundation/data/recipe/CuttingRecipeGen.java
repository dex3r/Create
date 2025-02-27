package com.simibubi.create.foundation.data.recipe;

import java.util.Objects;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllTags;
import com.simibubi.create.foundation.data.recipe.CreateRecipeProvider.GeneratedRecipe;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

@SuppressWarnings("unused")
public class CuttingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe
		ANDESITE_ALLOY = create(I::andesiteAlloy, b -> b.duration(200)
			.output(AllBlocks.SHAFT.get(), 6)),

		BAMBOO_PLANKS = create(() -> Blocks.BAMBOO_PLANKS, b -> b.duration(20)
			.output(Blocks.BAMBOO_MOSAIC, 1)),
		BAMBOO_BLOCK = stripAndMakePlanks(Blocks.BAMBOO_BLOCK, Blocks.STRIPPED_BAMBOO_BLOCK, Blocks.BAMBOO_PLANKS, 3),

		/*
		 * Mod compat
		 */

		// Ars Nouveau (all logs yield the same plank)
		ARS_N = cuttingCompat(Mods.ARS_N, "blue_archwood", "purple_archwood", "green_archwood", "red_archwood"),

		// Regions Unexplored
		RU_14 = stripOnlyDiffModId(Mods.RU, "silver_birch_log", Mods.MC, "stripped_birch_log"),
		RU_15 = stripOnlyDiffModId(Mods.RU, "silver_birch_wood", Mods.MC, "stripped_birch_wood"),

		// IE
		IE_WIRES = ieWires("copper", "electrum", "aluminum", "steel", "lead")
		;

	GeneratedRecipe stripAndMakePlanks(Block wood, Block stripped, Block planks) {
		return stripAndMakePlanks(wood, stripped, planks, 6);
	}

	GeneratedRecipe stripAndMakePlanks(Block wood, Block stripped, Block planks, int planksAmount) {
		create(() -> wood, b -> b.duration(50)
			.output(stripped));
		return create(() -> stripped, b -> b.duration(50)
			.output(planks, planksAmount));
	}

	GeneratedRecipe cuttingCompat(Mods mod, String... woodtypes) {
		for (String type : woodtypes) {
			String planks = type + "_planks";

			if (mod == Mods.ARS_N && type.contains("archwood"))
				planks = "archwood_planks";

			String strippedPre = mod.strippedIsSuffix ? "" : "stripped_";
			String strippedPost = mod.strippedIsSuffix ? "_stripped" : "";
			stripAndMakePlanks(mod, type + "_log", strippedPre + type + "_log" + strippedPost, planks);

			String wood = type + (mod.omitWoodSuffix ? "" : "_wood");
			stripAndMakePlanks(mod, wood, strippedPre + wood + strippedPost, planks);
		}
		return null;
	}

	GeneratedRecipe cuttingCompatLogOnly(Mods mod, String... woodtypes) {
		for (String type : woodtypes) {
			String planks = type + "_planks";
			String strippedPre = mod.strippedIsSuffix ? "" : "stripped_";
			String strippedPost = mod.strippedIsSuffix ? "_stripped" : "";
			stripAndMakePlanks(mod, type + "_log", strippedPre + type + "_log" + strippedPost, planks);
		}
		return null;
	}

	GeneratedRecipe stripOnlyDiffModId(Mods mod1, String wood, Mods mod2, String stripped) {
		create("compat/" + mod1.getId() + "/" + wood, b -> b.duration(50)
				.require(mod1, wood)
				.output(1, mod2, stripped, 1)
				.whenModLoaded(mod1.getId()));
		return null;
	}

	GeneratedRecipe stripAndMakePlanks(Mods mod, String wood, String stripped, String planks) {
		if (wood != null)
			create("compat/" + mod.getId() + "/" + wood, b -> b.duration(50)
				.require(mod, wood)
				.output(1, mod, stripped, 1)
				.whenModLoaded(mod.getId()));
		if (planks != null)
			if (!Objects.equals(mod.getId(), Mods.VH.getId())) {
				create("compat/" + mod.getId() + "/" + stripped, b -> b.duration(50)
						.require(mod, stripped)
						.output(1, mod, planks, 6)
						.whenModLoaded(mod.getId()));
			} else {
				create("compat/" + mod.getId() + "/" + stripped, b -> b.duration(50)
						.require(mod, stripped)
						.output(1, mod, planks, 4)
						.whenModLoaded(mod.getId()));
			}
		return null;
	}

	GeneratedRecipe ieWires(String... metals) {
		for (String metal : metals)
			create(Mods.IE.recipeId("wire_" + metal), b -> b.duration(50)
				.require(AllTags.forgeItemTag("plates/" + metal))
				.output(1, Mods.IE, "wire_" + metal, 2)
				.whenModLoaded(Mods.IE.getId()));
		return null;
	}

	public CuttingRecipeGen(FabricDataOutput output) {
		super(output);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.CUTTING;
	}
}
