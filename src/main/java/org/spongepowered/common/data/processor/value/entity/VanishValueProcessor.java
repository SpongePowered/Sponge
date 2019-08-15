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
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.bridge.data.VanishableBridge;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;

public class VanishValueProcessor extends AbstractSpongeValueProcessor<VanishableBridge, Boolean, Value<Boolean>> {

    public VanishValueProcessor() {
        super(VanishableBridge.class, Keys.VANISH);
    }

    @Override
    protected Value<Boolean> constructValue(final Boolean actualValue) {
        return new SpongeValue<>(Keys.VANISH, false, actualValue);
    }

    @Override
    protected boolean set(final VanishableBridge container, final Boolean value) {
        if (container instanceof net.minecraft.entity.Entity && ((net.minecraft.entity.Entity) container).world.isRemote) {
            return false;
        }
        container.bridge$setVanished(value);
        return true;
    }

    @Override
    protected Optional<Boolean> getVal(final VanishableBridge container) {
        return Optional.of(container.bridge$isVanished());
    }

    @Override
    protected ImmutableValue<Boolean> constructImmutableValue(final Boolean value) {
        return ImmutableSpongeValue.cachedOf(Keys.VANISH, false, value);
    }

    @Override
    public DataTransactionResult removeFrom(final ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
