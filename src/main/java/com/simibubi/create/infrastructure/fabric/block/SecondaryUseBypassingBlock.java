package com.simibubi.create.infrastructure.fabric.block;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Interface implementable on Block classes that allows for determining if a player's secondary use should be respected.
 * Secondary use is triggered when a player is sneaking, and skips block use in favor of item use.
 */
public interface SecondaryUseBypassingBlock {
	/**
	 * @return true if secondary use should be bypassed
	 */
	boolean shouldBypassSecondaryUse(Player player, InteractionHand hand, BlockState state);
}
