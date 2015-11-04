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
package org.spongepowered.common.data.processor.value.entity;

import static org.spongepowered.common.data.util.ComparatorUtil.intComparator;

import net.minecraft.entity.item.EntityXPOrb;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeBoundedValue;
import org.spongepowered.common.data.value.mutable.SpongeBoundedValue;
import org.spongepowered.common.interfaces.entity.IMixinEntityXPOrb;

import java.util.Optional;

public class ExpOrbValueProcessor extends AbstractSpongeValueProcessor<EntityXPOrb, Integer, MutableBoundedValue<Integer>> {

    public ExpOrbValueProcessor() {
        super(EntityXPOrb.class, Keys.CONTAINED_EXPERIENCE);
    }

    @Override
    protected MutableBoundedValue<Integer> constructValue(Integer defaultValue) {
        return new SpongeBoundedValue<>(this.getKey(), 0, intComparator(), 0, Integer.MAX_VALUE, defaultValue);
    }

    @Override
    protected boolean set(EntityXPOrb container, Integer value) {
        ((IMixinEntityXPOrb) container).setExperience(value);
        return true;
    }

    @Override
    protected Optional<Integer> getVal(EntityXPOrb container) {
        return Optional.of(((IMixinEntityXPOrb) container).getExperience());
    }

    @Override
    protected ImmutableValue<Integer> constructImmutableValue(Integer value) {
        return new ImmutableSpongeBoundedValue<>(this.getKey(), value, 0, intComparator(), 0, Integer.MAX_VALUE);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionBuilder.failNoData();
    }

}
