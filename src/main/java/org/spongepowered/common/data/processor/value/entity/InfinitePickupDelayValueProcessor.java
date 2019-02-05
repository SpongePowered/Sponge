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

import net.minecraft.entity.item.EntityItem;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.util.DataConstants;
import org.spongepowered.common.data.value.SpongeMutableValue;
import org.spongepowered.common.interfaces.entity.item.IMixinEntityItem;

import java.util.Optional;

public class InfinitePickupDelayValueProcessor extends AbstractSpongeValueProcessor<EntityItem, Boolean> {

    public InfinitePickupDelayValueProcessor() {
        super(EntityItem.class, Keys.INFINITE_PICKUP_DELAY);
    }

    @Override
    public Value.Mutable<Boolean> constructMutableValue(Boolean defaultValue) {
        return new SpongeMutableValue<>(Keys.INFINITE_PICKUP_DELAY, defaultValue);
    }

    @Override
    protected boolean set(EntityItem container, Boolean value) {
        ((IMixinEntityItem) container).setPickupDelay(value ? DataConstants.Entity.Item.MAGIC_NO_PICKUP : DataConstants.Entity.Item.DEFAULT_PICKUP_DELAY, value);
        return true;
    }

    @Override
    protected Optional<Boolean> getVal(EntityItem container) {
        return Optional.of(((IMixinEntityItem) container).infinitePickupDelay());
    }

    @Override
    protected Value.Immutable<Boolean> constructImmutableValue(Boolean value) {
        return constructMutableValue(value).asImmutable();
    }

    @Override
    protected boolean supports(EntityItem container) {
        return true;
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

}
