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
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.mixin.core.tileentity.CommandBlockBaseLogicAccessor;

import java.util.Optional;
import net.minecraft.entity.item.minecart.MinecartCommandBlockEntity;

public class EntityCommandValueProcessor extends AbstractSpongeValueProcessor<MinecartCommandBlockEntity, String, Value<String>> {

    public EntityCommandValueProcessor() {
        super(MinecartCommandBlockEntity.class, Keys.COMMAND);
    }

    @Override
    public DataTransactionResult removeFrom(final ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected Value<String> constructValue(final String actualValue) {
        return new SpongeValue<>(Keys.COMMAND, actualValue);
    }

    @Override
    protected boolean set(final MinecartCommandBlockEntity container, final String value) {
        ((CommandBlockBaseLogicAccessor) container.func_145822_e()).accessor$setCommandStored(value);
        return true;
    }

    @Override
    protected Optional<String> getVal(final MinecartCommandBlockEntity container) {
        return Optional.of(container.func_145822_e().func_145753_i());
    }

    @Override
    protected ImmutableValue<String> constructImmutableValue(final String value) {
        return constructValue(value).asImmutable();
    }

}
