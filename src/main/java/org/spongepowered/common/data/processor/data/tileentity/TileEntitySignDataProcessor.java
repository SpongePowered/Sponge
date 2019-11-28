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
package org.spongepowered.common.data.processor.data.tileentity;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableSignData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeSignData;
import org.spongepowered.common.data.processor.common.AbstractTileEntitySingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeListValue;
import org.spongepowered.common.data.value.mutable.SpongeListValue;
import org.spongepowered.common.text.SpongeTexts;

import java.util.List;
import java.util.Optional;

public class TileEntitySignDataProcessor
        extends AbstractTileEntitySingleDataProcessor<SignTileEntity, List<Text>, ListValue<Text>, SignData, ImmutableSignData> {

    public TileEntitySignDataProcessor() {
        super(SignTileEntity.class, Keys.SIGN_LINES);
    }

    @Override
    protected Optional<List<Text>> getVal(SignTileEntity sign) {
        final ITextComponent[] rawLines = sign.field_145915_a;
        final List<Text> signLines = Lists.newArrayListWithExpectedSize(4);
        for (int i = 0; i < rawLines.length; i++) {
            signLines.add(i, rawLines[i] == null ? Text.EMPTY : SpongeTexts.toText(rawLines[i]));
        }
        return Optional.of(signLines);

    }

    @Override
    protected boolean set(SignTileEntity sign, List<Text> lines) {
        for (int i = 0; i < sign.field_145915_a.length; i++) {
            Text line = lines.size() > i ? lines.get(i) : Text.EMPTY;
            if (line == null) {
                throw new IllegalArgumentException("A null line was given at index " + i);
            }
            sign.field_145915_a[i] = SpongeTexts.toComponent(line);
        }
        sign.func_70296_d();
        ((ServerWorld) sign.func_145831_w()).func_184164_w().func_180244_a(sign.func_174877_v());
        return true;
    }

    @Override
    public Optional<SignData> fill(DataContainer container, SignData signData) {
        if (!container.contains(Keys.SIGN_LINES.getQuery())) {
            return Optional.empty();
        }
        checkNotNull(signData);
        final List<String> lines = container.getStringList(Keys.SIGN_LINES.getQuery()).get();
        final List<Text> textLines = Lists.newArrayListWithCapacity(4);
        try {
            for (int i = 0; i < 4; i++) {
                textLines.set(i, TextSerializers.JSON.deserialize(lines.get(i)));
            }
        } catch (Exception e) {
            throw new InvalidDataException("Could not translate text json lines", e);
        }
        return Optional.of(signData.set(Keys.SIGN_LINES, textLines));
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public ListValue<Text> constructValue(List<Text> defaultValue) {
        return new SpongeListValue<>(Keys.SIGN_LINES, defaultValue);
    }

    @Override
    protected ImmutableValue<List<Text>> constructImmutableValue(List<Text> value) {
        return new ImmutableSpongeListValue<>(Keys.SIGN_LINES, ImmutableList.copyOf(value));
    }

    @Override
    protected SignData createManipulator() {
        return new SpongeSignData();
    }

}
