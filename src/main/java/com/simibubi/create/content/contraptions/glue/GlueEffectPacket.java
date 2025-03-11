package com.simibubi.create.content.contraptions.glue;

import com.simibubi.create.AllPackets;
import net.createmod.catnip.net.base.ClientboundPacketPayload;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public record GlueEffectPacket(BlockPos pos, Direction direction, boolean fullBlock) implements ClientboundPacketPayload {
	public static final StreamCodec<ByteBuf, com.simibubi.create.content.contraptions.glue.GlueEffectPacket> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, com.simibubi.create.content.contraptions.glue.GlueEffectPacket::pos,
			Direction.STREAM_CODEC, com.simibubi.create.content.contraptions.glue.GlueEffectPacket::direction,
			ByteBufCodecs.BOOL, com.simibubi.create.content.contraptions.glue.GlueEffectPacket::fullBlock,
			com.simibubi.create.content.contraptions.glue.GlueEffectPacket::new
	);

	@Override
	@Environment(EnvType.CLIENT)
	public void handle(LocalPlayer player) {
		if (!player.blockPosition().closerThan(pos, 100))
			return;
		SuperGlueItem.spawnParticles(player.clientLevel, pos, direction, fullBlock);
	}
@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.GLUE_EFFECT;
	}
}
