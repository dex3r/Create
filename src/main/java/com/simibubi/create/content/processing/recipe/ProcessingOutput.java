package com.simibubi.create.content.processing.recipe;

import java.util.Random;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.createmod.catnip.data.Pair;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ProcessingOutput {

	public static final ProcessingOutput EMPTY = new ProcessingOutput(ItemStack.EMPTY, 1);

	public static final StreamCodec<RegistryFriendlyByteBuf, ProcessingOutput> STREAM_CODEC = StreamCodec.of(
			(b, i) -> i.write(b), ProcessingOutput::read
	);

	private static final Random r = new Random();
	private final ItemStack stack;
	private final float chance;

	private Pair<ResourceLocation, Integer> compatDatagenOutput;

	public ProcessingOutput(ItemStack stack, float chance) {
		this.stack = stack;
		this.chance = chance;
	}

	public ProcessingOutput(ItemStack stack, int count, float chance) {
		stack.setCount(count);

		this.stack = stack;
		this.chance = chance;
	}

	public ProcessingOutput(Pair<ResourceLocation, Integer> item, float chance) {
		this.stack = ItemStack.EMPTY;
		this.compatDatagenOutput = item;
		this.chance = chance;
	}

	private static ProcessingOutput fromCodec(Either<ItemStack, Pair<ResourceLocation, Integer>> item,
											  int count, float chance) {
		return item.map(
				stack -> new ProcessingOutput(stack, count, chance),
				compat -> new ProcessingOutput(compat, chance)
		);
	}

	public ItemStack getStack() {
		return stack;
	}

	private Either<ItemStack, Pair<ResourceLocation, Integer>> getCodecStack() {
		return compatDatagenOutput != null ? Either.right(compatDatagenOutput) : Either.left(getStack());
	}

	public float getChance() {
		return chance;
	}

	public ItemStack rollOutput() {
		int outputAmount = stack.getCount();
		for (int roll = 0; roll < stack.getCount(); roll++)
			if (r.nextFloat() > chance)
				outputAmount--;
		if (outputAmount == 0)
			return ItemStack.EMPTY;
		ItemStack out = stack.copy();
		out.setCount(outputAmount);
		return out;
	}

	private static final Codec<Pair<ResourceLocation, Integer>> COMPAT_CODEC = ResourceLocation.CODEC.comapFlatMap(
		loc -> DataResult.error(() -> "Compat cannot be deserialized"),
		Pair::getFirst
	);

	private static final Codec<Either<ItemStack, Pair<ResourceLocation, Integer>>> ITEM_CODEC = Codec.either(
		ItemStack.SINGLE_ITEM_CODEC,
		COMPAT_CODEC
	);

	public static final Codec<ProcessingOutput> CODEC = RecordCodecBuilder.create(i -> i.group(
		ITEM_CODEC.fieldOf("item").forGetter(ProcessingOutput::getCodecStack),
		Codec.INT.optionalFieldOf("count", 1).forGetter(s -> {
			if (s.compatDatagenOutput != null)
				return s.compatDatagenOutput.getSecond();
			return s.getStack().getCount();
		}),
		Codec.FLOAT.optionalFieldOf("chance", 1F).forGetter(s -> s.chance)
	).apply(i, ProcessingOutput::fromCodec));

	public void write(RegistryFriendlyByteBuf buf) {
		ItemStack.STREAM_CODEC.encode(buf, getStack());
		buf.writeFloat(getChance());
	}

	public static ProcessingOutput read(RegistryFriendlyByteBuf buf) {
		return new ProcessingOutput(ItemStack.STREAM_CODEC.decode(buf), buf.readFloat());
	}

}
