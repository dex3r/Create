package com.simibubi.create.content.redstone.displayLink;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.CatnipClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class ClickToLinkBlockItem extends BlockItem {

	public ClickToLinkBlockItem(Block pBlock, Properties pProperties) {
		super(pBlock, pProperties);
	}

	public static InteractionResult linkableItemAlwaysPlacesWhenUsed(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
		ItemStack usedItem = player.getItemInHand(hand);
		if (!(usedItem.getItem() instanceof ClickToLinkBlockItem blockItem))
			return InteractionResult.PASS;
		if (level.getBlockState(hit.getBlockPos()).is(blockItem.getBlock()))
			return InteractionResult.PASS;

		return InteractionResult.FAIL;
	}

	@Override
	public InteractionResult useOn(UseOnContext pContext) {
		ItemStack stack = pContext.getItemInHand();
		BlockPos pos = pContext.getClickedPos();
		Level level = pContext.getLevel();
		BlockState state = level.getBlockState(pos);
		Player player = pContext.getPlayer();
		String msgKey = getMessageTranslationKey();
		int maxDistance = getMaxDistanceFromSelection();

		if (player == null)
			return InteractionResult.FAIL;

		if (player.isShiftKeyDown() && stack.hasTag()) {
			if (level.isClientSide)
				return InteractionResult.SUCCESS;
			player.displayClientMessage(CreateLang.translateDirect(msgKey + ".clear"), true);
			stack.setTag(null);
			return InteractionResult.SUCCESS;
		}

		String placedDim = level.dimension()
			.location()
			.toString();

		if (!stack.hasTag()) {
			if (!isValidTarget(level, pos)) {
				if (placeWhenInvalid()) {
					InteractionResult useOn = super.useOn(pContext);
					if (level.isClientSide || useOn == InteractionResult.FAIL)
						return useOn;

					ItemStack itemInHand = player.getItemInHand(pContext.getHand());
					if (!itemInHand.isEmpty())
						itemInHand.setTag(null);
					return useOn;
				}

				if (level.isClientSide)
					AllSoundEvents.DENY.playFrom(player);
				player.displayClientMessage(CreateLang.translateDirect(msgKey + ".invalid"), true);
				return InteractionResult.FAIL;
			}

			if (level.isClientSide)
				return InteractionResult.SUCCESS;
			CompoundTag stackTag = stack.getOrCreateTag();
			stackTag.put("SelectedPos", NbtUtils.writeBlockPos(pos));
			stackTag.putString("SelectedDimension", placedDim);

			player.displayClientMessage(CreateLang.translateDirect(msgKey + ".set"), true);
			stack.setTag(stackTag);
			return InteractionResult.SUCCESS;
		}

		CompoundTag tag = stack.getTag();
		CompoundTag teTag = new CompoundTag();

		BlockPos selectedPos = NbtUtils.readBlockPos(tag.getCompound("SelectedPos"));
		String selectedDim = tag.getString("SelectedDimension");
		BlockPos placedPos = pos.relative(pContext.getClickedFace(), state.canBeReplaced() ? 0 : 1);

		if (maxDistance != -1 && (!selectedPos.closerThan(placedPos, maxDistance) || !selectedDim.equals(placedDim))) {
			player.displayClientMessage(CreateLang.translateDirect(msgKey + ".too_far")
				.withStyle(ChatFormatting.RED), true);
			return InteractionResult.FAIL;
		}

		teTag.put("TargetOffset", NbtUtils.writeBlockPos(selectedPos.subtract(placedPos)));
		teTag.putString("TargetDimension", selectedDim);
		tag.put("BlockEntityTag", teTag);

		InteractionResult useOn = super.useOn(pContext);
		if (level.isClientSide || useOn == InteractionResult.FAIL)
			return useOn;

		ItemStack itemInHand = player.getItemInHand(pContext.getHand());
		if (!itemInHand.isEmpty())
			itemInHand.setTag(null);
		player.displayClientMessage(CreateLang.translateDirect(msgKey + ".success")
			.withStyle(ChatFormatting.GREEN), true);
		return useOn;
	}

	private static BlockPos lastShownPos = null;
	private static AABB lastShownAABB = null;

	@Environment(EnvType.CLIENT)
	public static void clientTick() {
		Player player = Minecraft.getInstance().player;
		if (player == null)
			return;
		ItemStack heldItemMainhand = player.getMainHandItem();
		if (!(heldItemMainhand.getItem() instanceof ClickToLinkBlockItem blockItem))
			return;
		if (!heldItemMainhand.hasTag())
			return;
		CompoundTag stackTag = heldItemMainhand.getOrCreateTag();
		if (!stackTag.contains("SelectedPos"))
			return;

		BlockPos selectedPos = NbtUtils.readBlockPos(stackTag.getCompound("SelectedPos"));

		if (!selectedPos.equals(lastShownPos)) {
			lastShownAABB = blockItem.getSelectionBounds(selectedPos);
			lastShownPos = selectedPos;
		}

		CatnipClient.OUTLINER.showAABB("target", lastShownAABB)
			.colored(0xffcb74)
			.lineWidth(1 / 16f);
	}

	public abstract int getMaxDistanceFromSelection();

	public abstract String getMessageTranslationKey();

	public boolean placeWhenInvalid() {
		return false;
	}

	public boolean isValidTarget(LevelAccessor level, BlockPos pos) {
		return true;
	}

	@Environment(EnvType.CLIENT)
	public AABB getSelectionBounds(BlockPos pos) {
		Level world = Minecraft.getInstance().level;
		BlockState state = world.getBlockState(pos);
		VoxelShape shape = state.getShape(world, pos);
		return shape.isEmpty() ? new AABB(BlockPos.ZERO)
			: shape.bounds()
				.move(pos);
	}

}
