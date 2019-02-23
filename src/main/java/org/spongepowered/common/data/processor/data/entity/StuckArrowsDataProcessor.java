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

import static com.google.common.base.Preconditions.checkArgument;

import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableStuckArrowsData;
import org.spongepowered.api.data.manipulator.mutable.StuckArrowsData;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeStuckArrowsData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.util.ComparatorUtil;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.data.value.SpongeImmutableBoundedValue;

import java.util.Optional;

public class StuckArrowsDataProcessor extends
        AbstractEntitySingleDataProcessor<EntityLivingBase, Integer, StuckArrowsData, ImmutableStuckArrowsData> {

    public StuckArrowsDataProcessor() {
        super(EntityLivingBase.class, Keys.STUCK_ARROWS);
    }

    @Override
    protected Value.Mutable<Integer> constructMutableValue(Integer actualValue) {
        return SpongeValueFactory.boundedBuilder(this.key)
                .minimum(0)
                .maximum(Integer.MAX_VALUE)
                .value(actualValue)
                .build();
    }

    @Override
    protected boolean set(EntityLivingBase entity, Integer arrows) {
        checkArgument(arrows >= 0, "Stuck arrows must be greater than or equal to zero");
        entity.setArrowCountInEntity(arrows);
        return true;
    }

    @Override
    protected Optional<Integer> getVal(EntityLivingBase entity) {
        return Optional.of(entity.getArrowCountInEntity());
    }

    @Override
    protected Value.Immutable<Integer> constructImmutableValue(Integer value) {
        return new SpongeImmutableBoundedValue<>(this.key, value, 0, Integer.MAX_VALUE, ComparatorUtil.intComparator());
    }

    @Override
    protected StuckArrowsData createManipulator() {
        return new SpongeStuckArrowsData();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

}
