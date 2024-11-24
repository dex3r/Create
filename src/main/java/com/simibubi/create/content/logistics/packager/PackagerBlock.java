package com.simibubi.create.content.logistics.packager;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.WrenchableDirectionalBlock;
import com.simibubi.create.foundation.utility.CreateLang;

import io.github.fabricators_of_create.porting_lib.block.NeighborChangeListeningBlock;
import net.createmod.catnip.utility.lang.Lang;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

public class PackagerBlock extends WrenchableDirectionalBlock implements IBE<PackagerBlockEntity>, IWrenchable, NeighborChangeListeningBlock {

	public static final EnumProperty<PackagerType> TYPE = EnumProperty.create("type", PackagerType.class);
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public enum PackagerType implements StringRepresentable {
		REGULAR, DEFRAG;

		@Override
		public String getSerializedName() {
			return Lang.asId(name());
		}
	}

	public PackagerBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(TYPE, PackagerType.REGULAR)
			.setValue(POWERED, false));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Capability<IItemHandler> itemCap = ForgeCapabilities.ITEM_HANDLER;
		Direction preferredFacing = null;
		for (Direction face : context.getNearestLookingDirections()) {
			BlockEntity be = context.getLevel()
				.getBlockEntity(context.getClickedPos()
					.relative(face));
			if (be instanceof PackagerBlockEntity)
				continue;
			if (be != null && (be.getCapability(itemCap)
				.isPresent())) {
				preferredFacing = face.getOpposite();
				break;
			}
		}

		if (preferredFacing == null) {
			Direction facing = context.getNearestLookingDirection();
			preferredFacing = context.getPlayer() != null && context.getPlayer()
				.isShiftKeyDown() ? facing : facing.getOpposite();
		}

		return super.getStateForPlacement(context).setValue(POWERED, context.getLevel()
			.hasNeighborSignal(context.getClickedPos()))
			.setValue(FACING, preferredFacing);
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
		BlockHitResult hit) {
		if (player == null)
			return InteractionResult.PASS;

		ItemStack itemInHand = player.getItemInHand(handIn);
		if (AllItems.WRENCH.isIn(itemInHand))
			return InteractionResult.PASS;
		if (AllBlocks.FACTORY_GAUGE.isIn(itemInHand))
			return InteractionResult.PASS;

		if (onBlockEntityUse(worldIn, pos, be -> {
			if (be.heldBox.isEmpty()) {
				if (be.animationTicks > 0)
					return InteractionResult.SUCCESS;
				if (itemInHand.getItem() instanceof PackageItem) {
					if (worldIn.isClientSide())
						return InteractionResult.SUCCESS;
					if (!be.unwrapBox(itemInHand.copy(), true))
						return InteractionResult.SUCCESS;
					be.unwrapBox(itemInHand.copy(), false);
					be.triggerStockCheck();
					itemInHand.shrink(1);
					if (itemInHand.isEmpty())
						player.setItemInHand(handIn, ItemStack.EMPTY);
					return InteractionResult.SUCCESS;
				}
				return InteractionResult.PASS;
			}
			if (be.animationTicks > 0)
				return InteractionResult.PASS;
			if (!worldIn.isClientSide()) {
				player.getInventory()
					.placeItemBackInInventory(be.heldBox.copy());
				be.heldBox = ItemStack.EMPTY;
				be.notifyUpdate();
			}
			return InteractionResult.SUCCESS;
		}).consumesAction())
			return InteractionResult.SUCCESS;

		return InteractionResult.PASS;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(POWERED, TYPE));
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		if (level.isClientSide)
			return InteractionResult.SUCCESS;

		level.setBlockAndUpdate(pos, state.cycle(TYPE));

		withBlockEntityDo(level, pos, pte -> {
			Player player = context.getPlayer();
			PackagerType value = state.cycle(TYPE)
				.getValue(TYPE);
			pte.defragmenterActive = value == PackagerType.DEFRAG;

			if (player != null)
				player.displayClientMessage(
					CreateLang.translateDirect("packager.mode_change." + value.getSerializedName()), true);
		});

		return InteractionResult.SUCCESS;
	}

	@Override
	public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
		if (neighbor.relative(state.getOptionalValue(FACING)
			.orElse(Direction.UP))
			.equals(pos))
			withBlockEntityDo(level, pos, PackagerBlockEntity::triggerStockCheck);
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
		boolean isMoving) {
		if (worldIn.isClientSide)
			return;
		boolean previouslyPowered = state.getValue(POWERED);
		if (previouslyPowered == worldIn.hasNeighborSignal(pos))
			return;
		worldIn.setBlock(pos, state.cycle(POWERED), 2);
		if (!previouslyPowered)
			withBlockEntityDo(worldIn, pos, PackagerBlockEntity::activate);
	}

	@Override
	public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
		IBE.onRemove(pState, pLevel, pPos, pNewState);
	}

	@Override
	public boolean shouldCheckWeakPower(BlockState state, SignalGetter level, BlockPos pos, Direction side) {
		return false;
	}

	@Override
	public Class<PackagerBlockEntity> getBlockEntityClass() {
		return PackagerBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends PackagerBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.PACKAGER.get();
	}

}
