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
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableExpOrbData;
import org.spongepowered.api.data.manipulator.mutable.ExpOrbData;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeExpOrbData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.interfaces.entity.IMixinEntityXPOrb;

import java.util.Optional;

public class ExpOrbDataProcessor extends
        AbstractEntitySingleDataProcessor<EntityXPOrb, Integer, ExpOrbData, ImmutableExpOrbData> {

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
    protected Value.Immutable<Integer> constructImmutableValue(Integer value) {
        return constructMutableValue(value).asImmutable();
    }

    @Override
    protected ExpOrbData createManipulator() {
        return new SpongeExpOrbData();
    }

    @Override
    protected Value.Mutable<Integer> constructMutableValue(Integer actualValue) {
        return SpongeValueFactory.boundedBuilder(Keys.CONTAINED_EXPERIENCE)
                .minimum(0)
                .maximum(Integer.MAX_VALUE)
                .value(actualValue)
                .build();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
