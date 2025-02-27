package com.simibubi.create.foundation.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@Mixin(LocalPlayer.class)
public abstract class HeavyBootsOnPlayerMixin extends AbstractClientPlayer {
	private HeavyBootsOnPlayerMixin(ClientLevel level, GameProfile profile) {
		super(level, profile);
	}

	@ModifyReturnValue(method = "isUnderWater()Z", at = @At("RETURN"))
	private boolean create$noSwimmingWithHeavyBootsOn(boolean original) {
		return getCustomData().contains("HeavyBoots") || original;
	}
}
