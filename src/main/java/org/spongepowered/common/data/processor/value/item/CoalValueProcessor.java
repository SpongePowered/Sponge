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
package org.spongepowered.common.data.processor.value.item;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.CoalType;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.item.SpongeCoalType;

import java.util.List;
import java.util.Optional;

public class CoalValueProcessor extends AbstractSpongeValueProcessor<ItemStack, CoalType, Value<CoalType>> {

    public CoalValueProcessor() {
        super(ItemStack.class, Keys.COAL_TYPE);
    }
    
    @Override
    protected boolean supports(ItemStack container) {
        return container.getItem() == Items.coal;
    }

    @Override
    protected Value<CoalType> constructValue(CoalType defaultValue) {
        return new SpongeValue<>(Keys.COAL_TYPE, defaultValue);
    }
    
    @Override
    protected ImmutableValue<CoalType> constructImmutableValue(CoalType defaultValue) {
        return new ImmutableSpongeValue<>(Keys.COAL_TYPE, defaultValue);
    }

    @Override
    public Optional<CoalType> getVal(ItemStack stack) {
        final int coalmeta = stack.getItemDamage();
        final List<CoalType> coalTypes = (List<CoalType>) Sponge.getRegistry().getAllOf(CoalType.class);
        return Optional.ofNullable(coalTypes.get(coalmeta));
    }

    @Override
    public boolean set(ItemStack stack, CoalType value) {
        stack.setItemDamage(((SpongeCoalType) value).type);
        return true;
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionBuilder.failNoData();
    }
}
