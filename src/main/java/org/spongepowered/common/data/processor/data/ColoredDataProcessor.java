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
package org.spongepowered.common.data.processor.data;

import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableColoredData;
import org.spongepowered.api.data.manipulator.mutable.ColoredData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.manipulator.mutable.SpongeColoredData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.util.ColorUtil;

import java.awt.Color;
import java.util.Optional;

public class ColoredDataProcessor extends AbstractItemSingleDataProcessor<Color, Value<Color>, ColoredData, ImmutableColoredData> {

    public ColoredDataProcessor() {
        super(ColorUtil::hasColor, Keys.COLOR);
    }

    @Override
    protected boolean supports(ItemStack stack) {
        return ColorUtil.hasColor(stack);
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        if (supports(dataHolder)) {
            final Optional<ColoredData> optional = from(dataHolder);
            final ItemStack stack = (ItemStack) dataHolder;
            if (ColorUtil.hasColorInNbt(stack) && optional.isPresent()) {
                final DataTransactionBuilder builder = DataTransactionBuilder.builder();
                try {
                    NbtDataUtil.removeColorFromNBT(stack);
                    return builder.replace(optional.get().getValues()).result(DataTransactionResult.Type.SUCCESS).build();
                } catch (Exception e) {
                    SpongeImpl.getLogger().error("There was an issue removing the color from an ItemStack!", e);
                    return builder.result(DataTransactionResult.Type.ERROR).build();
                }
            } else {
                return DataTransactionBuilder.failNoData();
            }
        }
        return DataTransactionBuilder.failNoData();
    }

    @Override
    protected boolean set(ItemStack itemStack, Color value) {
        if (!this.supports(itemStack)) {
            return false;
        }
        ColorUtil.setItemStackColor(itemStack, value);
        return true;
    }

    @Override
    protected Optional<Color> getVal(ItemStack itemStack) {
        if (!this.supports(itemStack)) {
            return Optional.empty();
        }
        return ColorUtil.getItemStackColor(itemStack);
    }

    @Override
    protected ImmutableValue<Color> constructImmutableValue(Color value) {
        return new ImmutableSpongeValue<>(Keys.COLOR, value);
    }

    @Override
    protected ColoredData createManipulator() {
        return new SpongeColoredData();
    }

}
