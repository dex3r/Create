package com.simibubi.create.content.kinetics.mechanicalArm;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.jetbrains.annotations.UnmodifiableView;

import com.simibubi.create.api.registry.CreateBuiltInRegistries;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public abstract class ArmInteractionPointType {
	private static List<ArmInteractionPointType> sortedTypes = null;
	@UnmodifiableView
	private static List<ArmInteractionPointType> sortedTypesView = null;

	public static void forEach(Consumer<ArmInteractionPointType> action) {
		getSorted().forEach(action);
	}

	@UnmodifiableView
	public static List<ArmInteractionPointType> getSorted() {
		if (sortedTypes == null) {
			sortedTypes = new ReferenceArrayList<>();

			CreateBuiltInRegistries.ARM_INTERACTION_POINT_TYPE.forEach(sortedTypes::add);
			sortedTypes.sort((t1, t2) -> t2.getPriority() - t1.getPriority());

			sortedTypesView = Collections.unmodifiableList(sortedTypes);
		}

		return sortedTypesView;
	}

	@Nullable
	public static ArmInteractionPointType getPrimaryType(Level level, BlockPos pos, BlockState state) {
		for (ArmInteractionPointType type : getSorted())
			if (type.canCreatePoint(level, pos, state))
				return type;
		return null;
	}

	public abstract boolean canCreatePoint(Level level, BlockPos pos, BlockState state);

	@Nullable
	public abstract ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state);

	public int getPriority() {
		return 0;
	}
}
