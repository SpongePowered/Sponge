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

import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.Value.Immutable;
import org.spongepowered.api.data.value.Value.Mutable;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.mixin.accessor.entity.item.FallingBlockEntityAccessor;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

public class FallingBlockCanHurtEntitiesValueProcessor extends AbstractSpongeValueProcessor<FallingBlockEntityAccessor, Boolean, Mutable<Boolean>> {

    public FallingBlockCanHurtEntitiesValueProcessor() {
        super(FallingBlockEntityAccessor.class, Keys.FALLING_BLOCK_CAN_HURT_ENTITIES);
    }

    @Override
    protected Mutable<Boolean> constructValue(final Boolean value) {
        return new SpongeValue<>(Keys.FALLING_BLOCK_CAN_HURT_ENTITIES, Constants.Entity.FallingBlock.DEFAULT_CAN_HURT_ENTITIES, value);
    }

    @Override
    protected boolean set(final FallingBlockEntityAccessor container, final Boolean value) {
        container.accessor$setHurtEntities(value);
        return true;
    }

    @Override
    protected Optional<Boolean> getVal(final FallingBlockEntityAccessor container) {
        return Optional.of(container.accessor$getHurtEntities());
    }

    @Override
    protected Immutable<Boolean> constructImmutableValue(final Boolean value) {
        return this.constructValue(value).asImmutable();
    }

    @Override
    public DataTransactionResult removeFrom(final ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
