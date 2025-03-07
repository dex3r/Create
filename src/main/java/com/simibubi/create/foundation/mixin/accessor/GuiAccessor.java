package com.simibubi.create.foundation.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

@Mixin(Gui.class)
public interface GuiAccessor {
	@Accessor("toolHighlightTimer")
	int create$getToolHighlightTimer();

	@Invoker("renderTextureOverlay")
	void create$renderTextureOverlay(GuiGraphics guiGraphics, ResourceLocation shaderLocation, float alpha);
}
