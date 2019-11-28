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

import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableRotationalData;
import org.spongepowered.api.data.manipulator.mutable.RotationalData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.api.util.rotation.Rotations;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.manipulator.mutable.SpongeRotationalData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;
import net.minecraft.entity.item.ItemFrameEntity;

public class RotationalDataProcessor
        extends AbstractEntitySingleDataProcessor<ItemFrameEntity, Rotation, Value<Rotation>, RotationalData, ImmutableRotationalData> {

    public RotationalDataProcessor() {
        super(ItemFrameEntity.class, Keys.ROTATION);
    }

    @Override
    protected boolean set(ItemFrameEntity entity, Rotation value) {
        entity.setItemRotation(value.getAngle() / 45);
        return true;
    }

    @Override
    protected Optional<Rotation> getVal(ItemFrameEntity entity) {
        return Optional.of(SpongeImpl.getGame().getRegistry().getRotationFromDegree(entity.getRotation() * 45).get());
    }

    @Override
    protected ImmutableValue<Rotation> constructImmutableValue(Rotation value) {
        return ImmutableSpongeValue.cachedOf(Keys.ROTATION, Rotations.BOTTOM, value);
    }

    @Override
    protected RotationalData createManipulator() {
        return new SpongeRotationalData();
    }

    @Override
    protected Value<Rotation> constructValue(Rotation actualValue) {
        return new SpongeValue<>(Keys.ROTATION, Rotations.BOTTOM, actualValue);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
