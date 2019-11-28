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

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableCoalData;
import org.spongepowered.api.data.manipulator.mutable.item.CoalData;
import org.spongepowered.api.data.type.CoalType;
import org.spongepowered.api.data.type.CoalTypes;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeCoalData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.item.SpongeCoalType;

import java.util.List;
import java.util.Optional;

public class CoalDataProcessor extends AbstractItemSingleDataProcessor<CoalType, Value<CoalType>, CoalData, ImmutableCoalData> {

    public CoalDataProcessor() {
        super(stack -> stack.getItem().equals(Items.COAL), Keys.COAL_TYPE);
    }

    @Override
    protected CoalData createManipulator() {
        return new SpongeCoalData();
    }

    @Override
    protected boolean set(ItemStack itemStack, CoalType value) {
        itemStack.func_77964_b(((SpongeCoalType) value).type);
        return true;
    }

    @Override
    protected Optional<CoalType> getVal(ItemStack itemStack) {
        final int coalmeta = itemStack.getDamage();
        final List<CoalType> coalTypes = (List<CoalType>) SpongeImpl.getRegistry().getAllOf(CoalType.class);
        return Optional.ofNullable(coalTypes.get(coalmeta));
    }

    @Override
    protected Value<CoalType> constructValue(CoalType actualValue) {
        return new SpongeValue<>(Keys.COAL_TYPE, CoalTypes.COAL, actualValue);
    }

    @Override
    protected ImmutableValue<CoalType> constructImmutableValue(CoalType value) {
        return ImmutableSpongeValue.cachedOf(Keys.COAL_TYPE, CoalTypes.COAL, value);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

}
