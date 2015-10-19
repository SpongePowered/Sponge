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

import static com.google.common.base.Preconditions.checkNotNull;

import java.awt.Color;
import java.util.Optional;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableColoredData;
import org.spongepowered.api.data.manipulator.mutable.ColoredData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeColoredData;
import org.spongepowered.common.data.manipulator.mutable.SpongeColoredData;
import org.spongepowered.common.data.processor.common.AbstractSpongeDataProcessor;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.util.ColorUtil;

import net.minecraft.item.ItemStack;

public class ColoredDataProcessor extends AbstractSpongeDataProcessor<ColoredData, ImmutableColoredData> {

    @Override
    public boolean supports(DataHolder dataHolder) {
        return dataHolder instanceof ItemStack;
    }

    @Override
    public Optional<ColoredData> from(DataHolder dataHolder) {
        if (dataHolder instanceof ItemStack) {
            final ItemStack stack = (ItemStack) dataHolder;
            return ColorUtil.getItemStackColor(stack).map(SpongeColoredData::new);
        }
        return Optional.empty();
    }

    @Override
    public Optional<ColoredData> fill(DataHolder dataHolder, ColoredData manipulator, MergeFunction overlap) {
        if (supports(dataHolder)) {
            final ColoredData data = from(dataHolder).orElse(null);
            final ColoredData newData = checkNotNull(overlap.merge(checkNotNull(manipulator), data));
            final Color color = newData.color().get();
            return Optional.of(manipulator.set(Keys.COLOR, color));
        }
        return Optional.empty();
    }

    @Override
    public Optional<ColoredData> fill(DataContainer container, ColoredData colorData) {
        final Color color = DataUtil.getData(container, Keys.COLOR, Color.class);
        return Optional.of(colorData.set(Keys.COLOR, color));
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, ColoredData manipulator, MergeFunction function) {
        return DataTransactionBuilder.failResult(manipulator.getValues());
    }

    @Override
    public Optional<ImmutableColoredData> with(Key<? extends BaseValue<?>> key, Object value, ImmutableColoredData immutable) {
        if (key == Keys.COLOR) {
            return Optional.of(new ImmutableSpongeColoredData((Color) value));
        }
        return Optional.empty();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        if (dataHolder instanceof ItemStack) {
            final DataTransactionBuilder builder = DataTransactionBuilder.builder();
            final Optional<ColoredData> optional = from(dataHolder);
            final ItemStack stack = (ItemStack) dataHolder;
            if (ColorUtil.hasItemStackColor(stack) && optional.isPresent()) {
                try {
                    NbtDataUtil.removeColorFromNBT(stack);
                    return builder.replace(optional.get().getValues()).result(DataTransactionResult.Type.SUCCESS).build();
                } catch (Exception e) {
                    Sponge.getLogger().error("There was an issue removing the color from an ItemStack!", e);
                    return builder.result(DataTransactionResult.Type.ERROR).build();
                }
            } else {
                return builder.result(DataTransactionResult.Type.SUCCESS).build();
            }
        }
        return DataTransactionBuilder.failNoData();
    }

    @Override
    public Optional<ColoredData> createFrom(DataHolder dataHolder) {
        return from(dataHolder);
    }

}
