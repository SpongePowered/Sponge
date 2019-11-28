/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.data.processor.data.item;

import com.google.common.collect.Iterables;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableGoldenAppleData;
import org.spongepowered.api.data.manipulator.mutable.item.GoldenAppleData;
import org.spongepowered.api.data.type.GoldenApple;
import org.spongepowered.api.data.type.GoldenApples;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeGoldenAppleData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.item.SpongeGoldenApple;

import java.util.Optional;

public class GoldenAppleDataProcessor
        extends AbstractItemSingleDataProcessor<GoldenApple, Value<GoldenApple>, GoldenAppleData, ImmutableGoldenAppleData> {

    public GoldenAppleDataProcessor() {
        super(input -> input.getItem().equals(Items.GOLDEN_APPLE), Keys.GOLDEN_APPLE_TYPE);
    }

    @Override
    protected GoldenAppleData createManipulator() {
        return new SpongeGoldenAppleData();
    }

    @Override
    protected boolean set(ItemStack itemStack, GoldenApple value) {
        itemStack.setItemDamage(((SpongeGoldenApple) value).type);
        return true;
    }

    @Override
    protected Optional<GoldenApple> getVal(ItemStack itemStack) {
        return Optional.of(Iterables.get(SpongeImpl.getRegistry().getAllOf(GoldenApple.class), itemStack.getMetadata()));
    }

    @Override
    protected Value<GoldenApple> constructValue(GoldenApple actualValue) {
        return new SpongeValue<>(Keys.GOLDEN_APPLE_TYPE, GoldenApples.GOLDEN_APPLE, actualValue);
    }

    @Override
    protected ImmutableValue<GoldenApple> constructImmutableValue(GoldenApple value) {
        return ImmutableSpongeValue.cachedOf(Keys.GOLDEN_APPLE_TYPE, GoldenApples.GOLDEN_APPLE, value);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

}
