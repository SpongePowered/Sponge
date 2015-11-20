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

import net.minecraft.entity.monster.EntitySkeleton;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.SkeletonType;
import org.spongepowered.api.data.type.SkeletonTypes;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;

public class SkeletonTypeValueProcessor extends AbstractSpongeValueProcessor<EntitySkeleton, SkeletonType, Value<SkeletonType>> {

    public SkeletonTypeValueProcessor() {
        super(EntitySkeleton.class, Keys.SKELETON_TYPE);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionBuilder.failNoData();
    }

    @Override
    protected Value<SkeletonType> constructValue(SkeletonType defaultValue) {
        return new SpongeValue<>(getKey(), defaultValue, SkeletonTypes.NORMAL);
    }

    @Override
    protected boolean set(EntitySkeleton container, SkeletonType value) {
        container.setSkeletonType(value == SkeletonTypes.WITHER ? 1 : 0);
        return true;
    }

    @Override
    protected Optional<SkeletonType> getVal(EntitySkeleton container) {
        switch (container.getSkeletonType()) {
            case 0:
                return Optional.of(SkeletonTypes.NORMAL);
                
            case 1:
                return Optional.of(SkeletonTypes.WITHER);
                
            default:
                return Optional.empty();
        }
    }
    
    @Override
    protected ImmutableValue<SkeletonType> constructImmutableValue(SkeletonType value) {
        return ImmutableSpongeValue.cachedOf(getKey(), SkeletonTypes.NORMAL, value);
    }

}
