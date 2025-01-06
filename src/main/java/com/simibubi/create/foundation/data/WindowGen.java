package com.simibubi.create.foundation.data;

import static com.simibubi.create.Create.REGISTRATE;
import static com.simibubi.create.foundation.data.CreateRegistrate.connectedTextures;

import io.github.fabricators_of_create.porting_lib.models.generators.ConfiguredModel;
import io.github.fabricators_of_create.porting_lib.models.generators.ModelFile;
import io.github.fabricators_of_create.porting_lib.tags.Tags;

import java.util.function.Function;
import java.util.function.Supplier;

import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.Create;
import com.simibubi.create.content.decoration.palettes.ConnectedGlassBlock;
import com.simibubi.create.content.decoration.palettes.ConnectedGlassPaneBlock;
import com.simibubi.create.content.decoration.palettes.GlassPaneBlock;
import com.simibubi.create.content.decoration.palettes.WindowBlock;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.block.connected.GlassPaneCTBehaviour;
import com.simibubi.create.foundation.block.connected.HorizontalCTBehaviour;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;

public class WindowGen {

	private static Properties glassProperties(Properties p) {
		return p.isValidSpawn(WindowGen::never)
			.isRedstoneConductor(WindowGen::never)
			.isSuffocating(WindowGen::never)
			.isViewBlocking(WindowGen::never);
	}

	private static boolean never(BlockState p_235436_0_, BlockGetter p_235436_1_, BlockPos p_235436_2_) {
		return false;
	}

	private static Boolean never(BlockState p_235427_0_, BlockGetter p_235427_1_, BlockPos p_235427_2_,
		EntityType<?> p_235427_3_) {
		return false;
	}

	public static BlockEntry<WindowBlock> woodenWindowBlock(WoodType woodType, Block planksBlock) {
		return woodenWindowBlock(woodType, planksBlock, () -> RenderType::cutoutMipped, false);
	}

	public static BlockBuilder<WindowBlock, CreateRegistrate> randomisedWindowBlock(String name,
		Supplier<? extends ItemLike> ingredient, Supplier<Supplier<RenderType>> renderType, boolean translucent,
		Supplier<MapColor> color) {
		ResourceLocation end_texture = Create.asResource(palettesDir() + name + "_end");
		ResourceLocation side_texture = Create.asResource(palettesDir() + name);
		Function<Integer, ResourceLocation> ends = i -> Create.asResource(palettesDir() + name + "_" + i + "_end");
		return windowBlock(name, ingredient, null, renderType, translucent, n -> end_texture, n -> side_texture, color)
			.blockstate((c, p) -> p.simpleBlock(c.get(), ConfiguredModel.builder()
				.modelFile(p.models()
					.cubeColumn(c.getName() + "_1", side_texture, ends.apply(1)))
				.nextModel()
				.modelFile(p.models()
					.cubeColumn(c.getName() + "_2", side_texture, ends.apply(2)))
				.nextModel()
				.modelFile(p.models()
					.cubeColumn(c.getName() + "_3", side_texture, ends.apply(3)))
				.nextModel()
				.modelFile(p.models()
					.cubeColumn(c.getName() + "_4", side_texture, ends.apply(4)))
				.build()))
			.item()
			.model((c, p) -> p.cubeColumn(c.getName(), side_texture, ends.apply(1)))
			.build();
	}

	public static BlockEntry<WindowBlock> customWindowBlock(String name, Supplier<? extends ItemLike> ingredient,
		Supplier<CTSpriteShiftEntry> ct, Supplier<Supplier<RenderType>> renderType, boolean translucent,
		Supplier<MapColor> color) {
		NonNullFunction<String, ResourceLocation> end_texture = n -> Create.asResource(palettesDir() + name + "_end");
		NonNullFunction<String, ResourceLocation> side_texture = n -> Create.asResource(palettesDir() + n);
		return windowBlock(name, ingredient, ct, renderType, translucent, end_texture, side_texture, color).register();
	}

	public static BlockEntry<WindowBlock> woodenWindowBlock(WoodType woodType, Block planksBlock,
		Supplier<Supplier<RenderType>> renderType, boolean translucent) {
		String woodName = woodType.name();
		String name = woodName + "_window";
		NonNullFunction<String, ResourceLocation> end_texture =
			$ -> new ResourceLocation("block/" + woodName + "_planks");
		NonNullFunction<String, ResourceLocation> side_texture = n -> Create.asResource(palettesDir() + n);
		return windowBlock(name, () -> planksBlock, () -> AllSpriteShifts.getWoodenWindow(woodType), renderType,
			translucent, end_texture, side_texture, planksBlock::defaultMapColor).register();
	}

	public static BlockBuilder<WindowBlock, CreateRegistrate> windowBlock(String name,
		Supplier<? extends ItemLike> ingredient, Supplier<CTSpriteShiftEntry> ct,
		Supplier<Supplier<RenderType>> renderType, boolean translucent,
		NonNullFunction<String, ResourceLocation> endTexture, NonNullFunction<String, ResourceLocation> sideTexture,
		Supplier<MapColor> color) {
		return REGISTRATE.block(name, p -> new WindowBlock(p, translucent))
			.onRegister(ct == null ? $ -> {
			} : connectedTextures(() -> new HorizontalCTBehaviour(ct.get())))
			.addLayer(renderType)
			.recipe((c, p) -> ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, c.get(), 2)
				.pattern(" # ")
				.pattern("#X#")
				.define('#', ingredient.get())
				.define('X', DataIngredient.tag(Tags.Items.GLASS_COLORLESS))
				.unlockedBy("has_ingredient", RegistrateRecipeProvider.has(ingredient.get()))
				.save(p::accept))
			.initialProperties(() -> Blocks.GLASS)
			.properties(WindowGen::glassProperties)
			.properties(p -> p.mapColor(color.get()))
			.loot((t, g) -> t.dropWhenSilkTouch(g))
			.blockstate((c, p) -> p.simpleBlock(c.get(), p.models()
				.cubeColumn(c.getName(), sideTexture.apply(c.getName()), endTexture.apply(c.getName()))))
			.tag(BlockTags.IMPERMEABLE)
			.simpleItem();
	}

	public static BlockEntry<ConnectedGlassBlock> framedGlass(String name,
		Supplier<ConnectedTextureBehaviour> behaviour) {
		return REGISTRATE.block(name, ConnectedGlassBlock::new)
			.onRegister(connectedTextures(behaviour))
			.addLayer(() -> RenderType::cutout)
			.initialProperties(() -> Blocks.GLASS)
			.properties(WindowGen::glassProperties)
			.loot((t, g) -> t.dropWhenSilkTouch(g))
			.recipe((c, p) -> p.stonecutting(DataIngredient.tag(Tags.Items.GLASS_COLORLESS),
				RecipeCategory.BUILDING_BLOCKS, c::get))
			.blockstate((c, p) -> BlockStateGen.cubeAll(c, p, "palettes/", "framed_glass"))
			.tag(Tags.Blocks.GLASS_COLORLESS, BlockTags.IMPERMEABLE)
			.item()
			.tag(Tags.Items.GLASS_COLORLESS)
			.model((c, p) -> p.cubeColumn(c.getName(), p.modLoc(palettesDir() + c.getName()),
				p.modLoc("block/palettes/framed_glass")))
			.build()
			.register();
	}

	public static BlockEntry<ConnectedGlassPaneBlock> framedGlassPane(String name, Supplier<? extends Block> parent,
		Supplier<CTSpriteShiftEntry> ctshift) {
		ResourceLocation sideTexture = Create.asResource(palettesDir() + "framed_glass");
		ResourceLocation itemSideTexture = Create.asResource(palettesDir() + name);
		ResourceLocation topTexture = Create.asResource(palettesDir() + "framed_glass_pane_top");
		Supplier<Supplier<RenderType>> renderType = () -> RenderType::cutoutMipped;
		return connectedGlassPane(name, parent, ctshift, sideTexture, itemSideTexture, topTexture, renderType)
			.register();
	}

	public static BlockBuilder<ConnectedGlassPaneBlock, CreateRegistrate> customWindowPane(String name,
		Supplier<? extends Block> parent, Supplier<CTSpriteShiftEntry> ctshift,
		Supplier<Supplier<RenderType>> renderType) {
		ResourceLocation topTexture = Create.asResource(palettesDir() + name + "_pane_top");
		ResourceLocation sideTexture = Create.asResource(palettesDir() + name);
		return connectedGlassPane(name, parent, ctshift, sideTexture, sideTexture, topTexture, renderType);
	}

	public static BlockEntry<ConnectedGlassPaneBlock> woodenWindowPane(WoodType woodType,
		Supplier<? extends Block> parent) {
		return woodenWindowPane(woodType, parent, () -> RenderType::cutoutMipped);
	}

	public static BlockEntry<ConnectedGlassPaneBlock> woodenWindowPane(WoodType woodType,
		Supplier<? extends Block> parent, Supplier<Supplier<RenderType>> renderType) {
		String woodName = woodType.name();
		String name = woodName + "_window";
		ResourceLocation topTexture = new ResourceLocation("block/" + woodName + "_planks");
		ResourceLocation sideTexture = Create.asResource(palettesDir() + name);
		return connectedGlassPane(name, parent, () -> AllSpriteShifts.getWoodenWindow(woodType), sideTexture,
			sideTexture, topTexture, renderType).register();
	}

	public static BlockEntry<GlassPaneBlock> standardGlassPane(String name, Supplier<? extends Block> parent,
		ResourceLocation sideTexture, ResourceLocation topTexture, Supplier<Supplier<RenderType>> renderType) {
		NonNullBiConsumer<DataGenContext<Block, GlassPaneBlock>, RegistrateBlockstateProvider> stateProvider =
			(c, p) -> p.paneBlock(c.get(), sideTexture, topTexture);
		return glassPane(name, parent, sideTexture, topTexture, GlassPaneBlock::new, renderType, $ -> {
		}, stateProvider).register();
	}

	private static BlockBuilder<ConnectedGlassPaneBlock, CreateRegistrate> connectedGlassPane(String name,
		Supplier<? extends Block> parent, Supplier<CTSpriteShiftEntry> ctshift, ResourceLocation sideTexture,
		ResourceLocation itemSideTexture, ResourceLocation topTexture, Supplier<Supplier<RenderType>> renderType) {
		NonNullConsumer<? super ConnectedGlassPaneBlock> connectedTextures = ctshift == null ? $ -> {
		} : connectedTextures(() -> new GlassPaneCTBehaviour(ctshift.get()));
		String CGPparents = "block/connected_glass_pane/";
		String prefix = name + "_pane_";

		Function<RegistrateBlockstateProvider, ModelFile> post =
			getPaneModelProvider(CGPparents, prefix, "post", sideTexture, topTexture),
			side = getPaneModelProvider(CGPparents, prefix, "side", sideTexture, topTexture),
			sideAlt = getPaneModelProvider(CGPparents, prefix, "side_alt", sideTexture, topTexture),
			noSide = getPaneModelProvider(CGPparents, prefix, "noside", sideTexture, topTexture),
			noSideAlt = getPaneModelProvider(CGPparents, prefix, "noside_alt", sideTexture, topTexture);

		NonNullBiConsumer<DataGenContext<Block, ConnectedGlassPaneBlock>, RegistrateBlockstateProvider> stateProvider =
			(c, p) -> p.paneBlock(c.get(), post.apply(p), side.apply(p), sideAlt.apply(p), noSide.apply(p),
				noSideAlt.apply(p));

		return glassPane(name, parent, itemSideTexture, topTexture, ConnectedGlassPaneBlock::new, renderType,
			connectedTextures, stateProvider);
	}

	private static Function<RegistrateBlockstateProvider, ModelFile> getPaneModelProvider(String CGPparents,
		String prefix, String partial, ResourceLocation sideTexture, ResourceLocation topTexture) {
		return p -> p.models()
			.withExistingParent(prefix + partial, Create.asResource(CGPparents + partial))
			.texture("pane", sideTexture)
			.texture("edge", topTexture);
	}

	private static <G extends GlassPaneBlock> BlockBuilder<G, CreateRegistrate> glassPane(String name,
		Supplier<? extends Block> parent, ResourceLocation sideTexture, ResourceLocation topTexture,
		NonNullFunction<Properties, G> factory, Supplier<Supplier<RenderType>> renderType,
		NonNullConsumer<? super G> connectedTextures,
		NonNullBiConsumer<DataGenContext<Block, G>, RegistrateBlockstateProvider> stateProvider) {
		name += "_pane";

		return REGISTRATE.block(name, factory)
			.onRegister(connectedTextures)
			.addLayer(renderType)
			.initialProperties(() -> Blocks.GLASS_PANE)
			.properties(p -> p.mapColor(parent.get()
				.defaultMapColor()))
			.blockstate(stateProvider)
			.recipe((c, p) -> ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, c.get(), 16)
				.pattern("###")
				.pattern("###")
				.define('#', parent.get())
				.unlockedBy("has_ingredient", RegistrateRecipeProvider.has(parent.get()))
				.save(p::accept))
			.tag(Tags.Blocks.GLASS_PANES)
			.loot((t, g) -> t.dropWhenSilkTouch(g))
			.item()
			.tag(Tags.Items.GLASS_PANES)
			.model((c, p) -> p.withExistingParent(c.getName(), Create.asResource("item/pane"))
				.texture("pane", sideTexture)
				.texture("edge", topTexture))
			.build();
	}

	private static String palettesDir() {
		return "block/palettes/";
	}

}
