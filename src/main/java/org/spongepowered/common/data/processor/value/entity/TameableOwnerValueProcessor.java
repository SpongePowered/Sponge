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

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import net.minecraft.entity.passive.EntityTameable;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableOptionalValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.common.data.ValueProcessor;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.processor.data.entity.TameableDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeOptionalValue;
import org.spongepowered.common.data.value.mutable.SpongeOptionalValue;

import java.util.UUID;

public class TameableOwnerValueProcessor extends AbstractSpongeValueProcessor<Optional<UUID>, OptionalValue<UUID>> {

    public TameableOwnerValueProcessor() {
        super(Keys.TAMED_OWNER);
    }

    @Override
    protected OptionalValue<UUID> constructValue(Optional<UUID> defaultValue) {
        return new SpongeOptionalValue<UUID>(this.getKey(), defaultValue);
    }

    @Override
    public Optional<Optional<UUID>> getValueFromContainer(ValueContainer<?> container) {
        if(container instanceof EntityTameable) {
            final Optional<UUID> out = TameableDataProcessor.getTamer((EntityTameable) container);
            return Optional.of(out);
        }
        return Optional.absent();
    }

    @Override
    public boolean supports(ValueContainer<?> container) {
        return container instanceof EntityTameable;
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, Optional<UUID> value) {
        final ImmutableSpongeOptionalValue<UUID> proposedValue = new ImmutableSpongeOptionalValue<UUID>(Keys.TAMED_OWNER, value);

        if(container instanceof EntityTameable) {
            final DataTransactionBuilder builder = DataTransactionBuilder.builder();
            final Optional<UUID> tamer = TameableDataProcessor.getTamer((EntityTameable) container);
            final ImmutableValue<Optional<UUID>> oldTamer = this.getApiValueFromContainer(container).get().asImmutable();
            final ImmutableOptionalValue<UUID> newTamer = new ImmutableSpongeOptionalValue<UUID>(Keys.TAMED_OWNER, value);

            try {
                ((EntityTameable) container).setOwnerId(TameableDataProcessor.asString(value));
            } catch (Exception e) {
                return DataTransactionBuilder.errorResult(newTamer);
            }
            return builder.success(newTamer).replace(oldTamer).result(DataTransactionResult.Type.SUCCESS).build();
        }

        return DataTransactionBuilder.failResult(proposedValue);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionBuilder.failNoData();
    }
}
