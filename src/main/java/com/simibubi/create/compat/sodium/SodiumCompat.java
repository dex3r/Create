package com.simibubi.create.compat.sodium;

import java.util.function.Function;

import com.simibubi.create.Create;
import com.simibubi.create.compat.Mods;

import net.caffeinemc.mods.sodium.api.texture.SpriteUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

/**
 * Fixes the Mechanical Saw's sprite and lets players know when Indium isn't installed.
 */
public class SodiumCompat {
	public static final ResourceLocation SAW_TEXTURE = Create.asResource("block/saw_reversed");

	public static void init() {
		ModContainer container = FabricLoader.getInstance().getModContainer(Mods.SODIUM.id()).orElseThrow();

		if (!Mods.INDIUM.isLoaded()) {
			ClientPlayConnectionEvents.JOIN.register(SodiumCompat::sendNoIndiumWarning);
		}

		WorldRenderEvents.START.register(ctx -> {
			Function<ResourceLocation, TextureAtlasSprite> atlas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
			TextureAtlasSprite sawSprite = atlas.apply(SAW_TEXTURE);
			SpriteUtil.INSTANCE.markSpriteActive(sawSprite);
		});
	}

	public static void sendNoIndiumWarning(ClientPacketListener handler, PacketSender sender, Minecraft mc) {
		if (mc.player == null)
			return;

		MutableComponent text = ComponentUtils.wrapInSquareBrackets(Component.literal("WARN"))
				.withStyle(ChatFormatting.GOLD)
				.append(Component.literal(" Sodium is installed, but Indium is not. This will cause visual issues with Create!")
				)
				.withStyle(style -> style
						.withClickEvent(
								new ClickEvent(ClickEvent.Action.OPEN_URL, "https://modrinth.com/mod/indium")
						)
						.withHoverEvent(
								new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click here to open Indium's mod page"))
						)
				);

		mc.player.displayClientMessage(text, false);
	}
}
