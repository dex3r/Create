package com.simibubi.create;

import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class AllRegistries {
	public static final Registry<ArmInteractionPointType> ARM_INTERACTION_POINT_TYPES = registry(Keys.ARM_INTERACTION_POINT_TYPES);
	public static final Registry<FanProcessingType> FAN_PROCESSING_TYPES = registry(Keys.FAN_PROCESSING_TYPES);
	public static final Registry<ItemAttributeType> ITEM_ATTRIBUTE_TYPES = registry(Keys.ITEM_ATTRIBUTE_TYPES);

	private static <T> Registry<T> registry(ResourceKey<Registry<T>> registryKey) {
		return FabricRegistryBuilder.createSimple(registryKey).buildAndRegister();
	}

	public static void register() {
	}

	public static final class Keys {
		public static final ResourceKey<Registry<ArmInteractionPointType>> ARM_INTERACTION_POINT_TYPES = key("arm_interaction_point_types");
		public static final ResourceKey<Registry<FanProcessingType>> FAN_PROCESSING_TYPES = key("fan_processing_types");
		public static final ResourceKey<Registry<ItemAttributeType>> ITEM_ATTRIBUTE_TYPES = key("item_attribute_types");

		private static <T> ResourceKey<Registry<T>> key(String name) {
			return ResourceKey.createRegistryKey(Create.asResource(name));
		}
	}
}
