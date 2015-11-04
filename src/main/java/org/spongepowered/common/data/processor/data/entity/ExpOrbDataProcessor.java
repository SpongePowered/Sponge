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
package org.spongepowered.common.data.processor.data.entity;

import net.minecraft.entity.item.EntityXPOrb;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableExpOrbData;
import org.spongepowered.api.data.manipulator.mutable.entity.ExpOrbData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeExpOrbData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeBoundedValue;
import org.spongepowered.common.interfaces.entity.IMixinEntityXPOrb;

import java.util.Optional;

import static org.spongepowered.common.data.util.ComparatorUtil.intComparator;

public class ExpOrbDataProcessor extends AbstractEntitySingleDataProcessor<EntityXPOrb, Integer, MutableBoundedValue<Integer>, ExpOrbData, ImmutableExpOrbData> {

    public ExpOrbDataProcessor() {
        super(EntityXPOrb.class, Keys.CONTAINED_EXPERIENCE);
    }

    @Override
    protected boolean set(EntityXPOrb entity, Integer value) {
        ((IMixinEntityXPOrb) entity).setExperience(value);
        return true;
    }

    @Override
    protected Optional<Integer> getVal(EntityXPOrb entity) {
        return Optional.of(((IMixinEntityXPOrb) entity).getExperience());
    }

    @Override
    protected ImmutableValue<Integer> constructImmutableValue(Integer value) {
        return new ImmutableSpongeBoundedValue<>(Keys.CONTAINED_EXPERIENCE, value, 0, intComparator(), 0, Integer.MAX_VALUE);
    }

    @Override
    protected ExpOrbData createManipulator() {
        return new SpongeExpOrbData();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionBuilder.failNoData();
    }
}
