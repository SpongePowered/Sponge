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

import net.minecraft.entity.monster.EntityEnderman;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableScreamingData;
import org.spongepowered.api.data.manipulator.mutable.entity.ScreamingData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeScreamingData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.mixin.core.entity.monster.EntityEndermanAccessor;

import java.util.Optional;

public class ScreamingDataProcessor
        extends AbstractEntitySingleDataProcessor<EntityEnderman, Boolean, Value<Boolean>, ScreamingData, ImmutableScreamingData> {

    public ScreamingDataProcessor() {
        super(EntityEnderman.class, Keys.IS_SCREAMING);
    }

    @Override
    protected ScreamingData createManipulator() {
        return new SpongeScreamingData(false);
    }

    @Override
    protected boolean set(final EntityEnderman entity, final Boolean value) {
        entity.func_184212_Q().func_187227_b(EntityEndermanAccessor.accessor$getScreamingParameter(), value);
        return true;
    }

    @Override
    protected Optional<Boolean> getVal(final EntityEnderman entity) {
        return Optional.of(entity.func_70823_r());
    }

    @Override
    protected Value<Boolean> constructValue(final Boolean actualValue) {
        return new SpongeValue<>(Keys.IS_SCREAMING, false, actualValue);
    }

    @Override
    protected ImmutableValue<Boolean> constructImmutableValue(final Boolean value) {
        return ImmutableSpongeValue.cachedOf(Keys.IS_SCREAMING, false, value);
    }

    @Override
    public DataTransactionResult removeFrom(final ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

}
