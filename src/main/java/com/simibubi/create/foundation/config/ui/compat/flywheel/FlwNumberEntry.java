package com.simibubi.create.foundation.config.ui.compat.flywheel;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import io.github.fabricators_of_create.porting_lib.mixin.accessors.client.accessor.AbstractWidgetAccessor;
import net.createmod.catnip.config.ui.ConfigTextField;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.TextStencilElement;
import net.createmod.catnip.utility.lang.Components;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.MutableComponent;

public abstract class FlwNumberEntry<T extends Number> extends FlwValueEntry<T> {
	protected int minOffset = 0, maxOffset = 0;
	protected TextStencilElement minText = null, maxText = null;
	protected EditBox textField;

	@Nullable
	public static FlwNumberEntry<? extends Number> create(String label, Supplier<?> getter, Consumer<?> option) {
		Object type = getter.get();
		if (type instanceof Integer) {
			return new FlwIntegerEntry(label, (Supplier<Integer>) getter, (Consumer<Integer>) option);
		} else if (type instanceof Float) {
			return new FlwFloatEntry(label, (Supplier<Float>) getter, (Consumer<Float>) option);
		} else if (type instanceof Double) {
			return new FlwDoubleEntry(label, (Supplier<Double>) getter, (Consumer<Double>) option);
		}

		return null;
	}

	public FlwNumberEntry(String label, Supplier<T> getter, Consumer<T> option) {
		super(label, getter, option);
		textField = new ConfigTextField(Minecraft.getInstance().font, 0, 0, 200, 20);
		if (this instanceof FlwIntegerEntry && annotations.containsKey("IntDisplay")) {
			String intDisplay = annotations.get("IntDisplay");
			int intValue = (Integer) getValue();
			String textValue;
			switch (intDisplay) {
				case "#":
					textValue = "#" + Integer.toHexString(intValue).toUpperCase(Locale.ROOT);
					break;
				case "0x":
					textValue = "0x" + Integer.toHexString(intValue).toUpperCase(Locale.ROOT);
					break;
				case "0b":
					textValue = "0b" + Integer.toBinaryString(intValue);
					break;
				default:
					textValue = String.valueOf(intValue);
			}
			textField.setValue(textValue);
		} else {
			textField.setValue(String.valueOf(getValue()));
		}
		textField.setTextColor(Theme.i(Theme.Key.TEXT));

		Object range = spec.getRange();
		try {
			Field minField = range.getClass().getDeclaredField("min");
			Field maxField = range.getClass().getDeclaredField("max");
			minField.setAccessible(true);
			maxField.setAccessible(true);
			T min = (T) minField.get(range);
			T max = (T) maxField.get(range);

			Font font = Minecraft.getInstance().font;
			if (min.doubleValue() > getTypeMin().doubleValue()) {
				MutableComponent t = Components.literal(formatBound(min) + " < ");
				minText = new TextStencilElement(font, t).centered(true, false);
				minText.withElementRenderer((ms, width, height, alpha) -> UIRenderHelper.angledGradient(ms, 0 ,0, height/2, height, width, Theme.p(Theme.Key.TEXT_DARKER)));
				minOffset = font.width(t);
			}
			if (max.doubleValue() < getTypeMax().doubleValue()) {
				MutableComponent t = Components.literal(" < " + formatBound(max));
				maxText = new TextStencilElement(font, t).centered(true, false);
				maxText.withElementRenderer((ms, width, height, alpha) -> UIRenderHelper.angledGradient(ms, 0 ,0, height/2, height, width, Theme.p(Theme.Key.TEXT_DARKER)));
				maxOffset = font.width(t);
			}
		} catch (NoSuchFieldException | IllegalAccessException | ClassCastException | NullPointerException ignored) {

		}

		textField.setResponder(s -> {
			try {
				T number = getParser().apply(s);

				textField.setTextColor(Theme.i(Theme.Key.TEXT));
				setValue(number);
			} catch (Throwable ignored) {
				textField.setTextColor(Theme.i(Theme.Key.BUTTON_FAIL));
			}
		});

		textField.moveCursorToStart();
		listeners.add(textField);
		onReset();
	}

	protected String formatBound(T bound) {
		String sci = String.format("%.2E", bound.doubleValue());
		String str = String.valueOf(bound);
		return sci.length() < str.length() ? sci : str;
	}

	protected abstract T getTypeMin();

	protected abstract T getTypeMax();

	protected abstract Function<String, T> getParser();

	@Override
	public void setEditable(boolean b) {
		super.setEditable(b);
		textField.setEditable(b);
	}

	@Override
	public void onValueChange(T newValue) {
		super.onValueChange(newValue);
	}

	@Override
	public void tick() {
		super.tick();
		textField.tick();
	}

	@Override
	public void render(GuiGraphics graphics, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean p_230432_9_, float partialTicks) {
		super.render(graphics, index, y, x, width, height, mouseX, mouseY, p_230432_9_, partialTicks);

		textField.setX(x + width - 82 - resetWidth);
		textField.setY(y + 8);
		textField.setWidth(Math.min(width - getLabelWidth(width) - resetWidth - minOffset - maxOffset, 40));
		((AbstractWidgetAccessor) textField).port_lib$setHeight(20);
		textField.render(graphics, mouseX, mouseY, partialTicks);

		if (minText != null)
			minText
					.at(textField.getX() - minOffset, textField.getY(), 0)
					.withBounds(minOffset, textField.getHeight())
					.render(graphics);

		if (maxText != null)
			maxText
					.at(textField.getX() + textField.getWidth(), textField.getY(), 0)
					.withBounds(maxOffset, textField.getHeight())
					.render(graphics);
	}

	public static class FlwIntegerEntry extends FlwNumberEntry<Integer> {

		public FlwIntegerEntry(String label, Supplier<Integer> getter, Consumer<Integer> option) {
			super(label, getter, option);
		}

		@Override
		protected Integer getTypeMin() {
			return Integer.MIN_VALUE;
		}

		@Override
		protected Integer getTypeMax() {
			return Integer.MAX_VALUE;
		}

		@Override
		protected Function<String, Integer> getParser() {
			return (string) -> {
				if (string.startsWith("#")) {
					return Integer.parseUnsignedInt(string.substring(1), 16);
				} else if (string.startsWith("0x")) {
					return Integer.parseUnsignedInt(string.substring(2), 16);
				} else if (string.startsWith("0b")) {
					return Integer.parseUnsignedInt(string.substring(2), 2);
				} else {
					return Integer.parseInt(string);
				}
			};
		}
	}

	public static class FlwFloatEntry extends FlwNumberEntry<Float> {

		public FlwFloatEntry(String label, Supplier<Float> getter, Consumer<Float> option) {
			super(label, getter, option);
		}

		@Override
		protected Float getTypeMin() {
			return -Float.MAX_VALUE;
		}

		@Override
		protected Float getTypeMax() {
			return Float.MAX_VALUE;
		}

		@Override
		protected Function<String, Float> getParser() {
			return Float::parseFloat;
		}
	}

	public static class FlwDoubleEntry extends FlwNumberEntry<Double> {

		public FlwDoubleEntry(String label, Supplier<Double> getter, Consumer<Double> option) {
			super(label, getter, option);
		}

		@Override
		protected Double getTypeMin() {
			return (double) -Float.MAX_VALUE;
		}

		@Override
		protected Double getTypeMax() {
			return (double) Float.MAX_VALUE;
		}

		@Override
		protected Function<String, Double> getParser() {
			return Double::parseDouble;
		}
	}
}
