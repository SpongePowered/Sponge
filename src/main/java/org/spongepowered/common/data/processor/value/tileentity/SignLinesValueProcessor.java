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
package org.spongepowered.common.data.processor.value.tileentity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.IChatComponent;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableListValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.util.DataConstants;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeListValue;
import org.spongepowered.common.data.value.mutable.SpongeListValue;
import org.spongepowered.common.text.SpongeTexts;

import java.util.List;
import java.util.Optional;

public class SignLinesValueProcessor extends AbstractSpongeValueProcessor<TileEntitySign, List<Text>, ListValue<Text>> {

    public SignLinesValueProcessor() {
        super(TileEntitySign.class, Keys.SIGN_LINES);
    }

    @Override
    public ListValue<Text> constructValue(List<Text> defaultValue) {
        return new SpongeListValue<>(Keys.SIGN_LINES, defaultValue);
    }

    @Override
    protected boolean set(TileEntitySign container, List<Text> value) {
        for (int i = 0; i < 4; i++) {
            container.signText[i] = SpongeTexts.toComponent(value.get(i));
        }
        container.markDirty();
        container.getWorld().markBlockForUpdate(container.getPos());
        return true;
    }

    @Override
    protected Optional<List<Text>> getVal(TileEntitySign container) {
        final IChatComponent[] rawLines = container.signText;
        final List<Text> signLines = Lists.newArrayListWithCapacity(4);
        for (int i = 0; i < rawLines.length; i++) {
            signLines.add(i, SpongeTexts.toText(rawLines[i]));
        }
        return Optional.of(signLines);
    }

    @Override
    protected ImmutableValue<List<Text>> constructImmutableValue(List<Text> value) {
        return new ImmutableSpongeListValue<>(Keys.SIGN_LINES, ImmutableList.copyOf(value));
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (container instanceof TileEntitySign) {
            final DataTransactionResult.Builder builder = DataTransactionResult.builder();
            final Optional<List<Text>> oldData = getValueFromContainer(container);
            if (oldData.isPresent()) {
                final ImmutableListValue<Text> immutableTexts = new ImmutableSpongeListValue<>(Keys.SIGN_LINES, ImmutableList.copyOf(oldData.get()));
                builder.replace(immutableTexts);
            }
            try {
                for (int i = 0; i < 4; i++) {
                    ((TileEntitySign) container).signText[i] = SpongeTexts.toComponent(Text.of());
                }
                ((TileEntitySign) container).markDirty();
                return builder.result(DataTransactionResult.Type.SUCCESS).build();
            } catch (Exception e) {
                return builder.result(DataTransactionResult.Type.ERROR).build();
            }
        }
        return DataTransactionResult.failNoData();
    }
}
