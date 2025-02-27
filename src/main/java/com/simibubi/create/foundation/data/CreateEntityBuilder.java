package com.simibubi.create.foundation.data;

import java.util.function.Predicate;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.builders.EntityBuilder;
import com.tterrag.registrate.fabric.EnvExecutor;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import dev.engine_room.flywheel.lib.visualization.SimpleEntityVisualizer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;


import net.fabricmc.api.EnvType;

@ParametersAreNonnullByDefault
public class CreateEntityBuilder<T extends Entity, P> extends EntityBuilder<T, P> {

	@Nullable
	private NonNullSupplier<SimpleEntityVisualizer.Factory<T>> visualFactory;
	private Predicate<T> renderNormally;

	public static <T extends Entity, P> EntityBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, EntityType.EntityFactory<T> factory, MobCategory classification) {
		return (new CreateEntityBuilder<>(owner, parent, name, callback, factory, classification)).defaultLang();
	}

	public CreateEntityBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, EntityType.EntityFactory<T> factory, MobCategory classification) {
		super(owner, parent, name, callback, factory, classification/*, (mobCategory, tEntityFactory) -> FabricEntityTypeBuilder.create(mobCategory, tEntityFactory)*/);
	}

	public CreateEntityBuilder<T, P> visual(NonNullSupplier<SimpleEntityVisualizer.Factory<T>> visualFactory) {
		return visual(visualFactory, true);
	}

	public CreateEntityBuilder<T, P> visual(NonNullSupplier<SimpleEntityVisualizer.Factory<T>> visualFactory, boolean renderNormally) {
		return visual(visualFactory, entity -> renderNormally);
	}

	public CreateEntityBuilder<T, P> visual(NonNullSupplier<SimpleEntityVisualizer.Factory<T>> visualFactory, Predicate<T> renderNormally) {
		if (this.visualFactory == null) {
			EnvExecutor.runWhenOn(EnvType.CLIENT, () -> this::registerVisualizer);
		}

		this.visualFactory = visualFactory;
		this.renderNormally = renderNormally;

		return this;
	}

	protected void registerVisualizer() {
		var visualFactory = this.visualFactory;
		if (visualFactory != null) {
			NonNullPredicate<T> renderNormally = this.renderNormally;
			SimpleEntityVisualizer.builder(getEntry())
					.factory(visualFactory.get())
					.skipVanillaRender(entity -> !renderNormally.test(entity))
					.apply();
		}
	}
}
