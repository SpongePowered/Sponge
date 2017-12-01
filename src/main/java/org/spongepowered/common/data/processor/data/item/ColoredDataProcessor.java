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

import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableColoredData;
import org.spongepowered.api.data.manipulator.mutable.ColoredData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.util.Color;
import org.spongepowered.common.data.manipulator.mutable.SpongeColoredData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;

public class ColoredDataProcessor extends AbstractItemSingleDataProcessor<Color, Value<Color>, ColoredData, ImmutableColoredData> {

    public ColoredDataProcessor() {
        super(ColoredDataProcessor::supportsColor, Keys.COLOR);
    }

    private static boolean supportsColor(ItemStack stack) {
        final Item item = stack.getItem();
        return item instanceof ItemArmor &&
                ((ItemArmor) item).getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER;
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (supports(container)) {
            ItemStack stack = (ItemStack) container;
            Optional<Color> old = getVal(stack);
            if (!old.isPresent()) {
                return DataTransactionResult.successNoData();
            }
            if (!NbtDataUtil.hasColorFromNBT(stack)) {
                return DataTransactionResult.failNoData();
            }
            NbtDataUtil.removeColorFromNBT(stack);
            return DataTransactionResult.successRemove(constructImmutableValue(old.get()));
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    protected boolean set(ItemStack container, Color value) {
        if (!supports(container)) {
            return false;
        }
        NbtDataUtil.setColorToNbt(container, value);
        return true;
    }

    @Override
    protected Optional<Color> getVal(ItemStack container) {
        // Special case for armor: it has a special method
        final Item item = container.getItem();
        if (item instanceof ItemArmor) {
            final int color = ((ItemArmor) item).getColor(container);
            return color == -1 ? Optional.empty() : Optional.of(Color.of(color));
        }
        return NbtDataUtil.getItemCompound(container).flatMap(NbtDataUtil::getColorFromNBT);
    }

    @Override
    protected Value<Color> constructValue(Color actualValue) {
        return new SpongeValue<>(Keys.COLOR, Color.BLACK, actualValue);
    }

    @Override
    protected ImmutableValue<Color> constructImmutableValue(Color value) {
        return ImmutableSpongeValue.cachedOf(Keys.COLOR, Color.BLACK, value);
    }

    @Override
    protected ColoredData createManipulator() {
        return new SpongeColoredData();
    }

}
