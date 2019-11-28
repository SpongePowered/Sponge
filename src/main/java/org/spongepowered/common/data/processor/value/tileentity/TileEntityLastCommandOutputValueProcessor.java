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

import net.minecraft.tileentity.TileEntityCommandBlock;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.mutable.SpongeOptionalValue;
import org.spongepowered.common.text.SpongeTexts;

import java.util.Optional;

public class TileEntityLastCommandOutputValueProcessor extends AbstractSpongeValueProcessor<TileEntityCommandBlock, Optional<Text>, OptionalValue<Text>> {

    public TileEntityLastCommandOutputValueProcessor() {
        super(TileEntityCommandBlock.class, Keys.LAST_COMMAND_OUTPUT);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected OptionalValue<Text> constructValue(Optional<Text> actualValue) {
        return new SpongeOptionalValue<>(Keys.LAST_COMMAND_OUTPUT, actualValue);
    }

    @Override
    protected boolean set(TileEntityCommandBlock container, Optional<Text> value) {
        container.func_145993_a().func_145750_b(SpongeTexts.toComponent(value.orElse(Text.of())));
        return true;
    }

    @Override
    protected Optional<Optional<Text>> getVal(TileEntityCommandBlock container) {
        Text text = SpongeTexts.toText(container.func_145993_a().func_145749_h());
        return Optional.of(Optional.of(text)); //#OptionalWrapping o.o
    }

    @Override
    protected ImmutableValue<Optional<Text>> constructImmutableValue(Optional<Text> value) {
        return constructValue(value).asImmutable();
    }

}
