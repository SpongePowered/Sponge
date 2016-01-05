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

import static org.spongepowered.common.data.util.ComparatorUtil.floatComparator;

import net.minecraft.entity.Entity;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.interfaces.entity.IMixinEntity;

import java.util.Optional;

public class ScaleValueProcessor extends AbstractSpongeValueProcessor<Entity, Float, MutableBoundedValue<Float>> {

    public ScaleValueProcessor() {
        super(Entity.class, Keys.SCALE);
    }

    @Override
    public MutableBoundedValue<Float> constructValue(Float scale) {
        return SpongeValueFactory.boundedBuilder(Keys.SCALE)
                .comparator(floatComparator())
                .minimum(0f)
                .maximum(Float.MAX_VALUE)
                .defaultValue(1f)
                .actualValue(scale)
                .build();
    }

    @Override
    protected boolean set(Entity container, Float value) {
        IMixinEntity mixinEntity = (IMixinEntity) container;
        mixinEntity.setSpongeSize(container.height, container.width, value);
        return false;
    }

    @Override
    protected Optional<Float> getVal(Entity container) {
        return Optional.of(1.0F);
    }

    @Override
    protected ImmutableBoundedValue<Float> constructImmutableValue(Float value) {
        return constructValue(value).asImmutable();
    }

    @Override
    public Optional<MutableBoundedValue<Float>> getApiValueFromContainer(ValueContainer<?> container) {
        if (container instanceof Entity) {
            return Optional.of(SpongeValueFactory.boundedBuilder(Keys.SCALE)
                    .minimum(0f)
                    .maximum(Float.MAX_VALUE)
                    .defaultValue(1f)
                    .actualValue(1.0F)
                    .build());
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(ValueContainer<?> container) {
        return container instanceof Entity;
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, Float value) {
        final ImmutableBoundedValue<Float> proposedValue = constructImmutableValue(value);
        if (container instanceof Entity) {
            Entity entity = (Entity) container;
            final DataTransactionResult.Builder builder = DataTransactionResult.builder();
            final ImmutableBoundedValue<Float> newScaleValue = SpongeValueFactory.boundedBuilder(Keys.SCALE)
                    .minimum(0f)
                    .maximum(Float.MAX_VALUE)
                    .defaultValue(1f)
                    .actualValue(value)
                    .build().asImmutable();
            final ImmutableBoundedValue<Float> oldScaleValue = getApiValueFromContainer(container).get().asImmutable();
            try {
                IMixinEntity mixinEntity = (IMixinEntity) entity;
                mixinEntity.setSpongeSize(entity.height, entity.width, value);
            } catch (Exception e) {
                return DataTransactionResult.errorResult(newScaleValue);
            }
            return builder.success(newScaleValue).replace(oldScaleValue).result(DataTransactionResult.Type.SUCCESS).build();
        }
        return DataTransactionResult.failResult(proposedValue);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
