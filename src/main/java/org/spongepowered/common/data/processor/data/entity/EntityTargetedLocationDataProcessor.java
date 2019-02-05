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

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.entity.Entity;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableTargetedLocationData;
import org.spongepowered.api.data.manipulator.mutable.TargetedLocationData;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.common.data.manipulator.mutable.SpongeTargetedLocationData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.SpongeImmutableValue;
import org.spongepowered.common.data.value.SpongeMutableValue;
import org.spongepowered.common.interfaces.ITargetedLocation;

import java.util.Optional;

public final class EntityTargetedLocationDataProcessor extends AbstractEntitySingleDataProcessor<Entity, Vector3d,
        TargetedLocationData, ImmutableTargetedLocationData> {

    public EntityTargetedLocationDataProcessor() {
        super(Entity.class, Keys.TARGETED_LOCATION);
    }

    @Override
    protected boolean set(Entity entity, Vector3d value) {
        if (entity instanceof ITargetedLocation) {
            ((ITargetedLocation) entity).setTargetedLocation(value);
            return true;
        }

        return false;
    }

    @Override
    protected Optional<Vector3d> getVal(Entity entity) {
        if (entity instanceof ITargetedLocation) {
            return Optional.of(((ITargetedLocation) entity).getTargetedLocation());
        }

        return Optional.empty();
    }

    @Override
    protected Value.Immutable<Vector3d> constructImmutableValue(Vector3d value) {
        return new SpongeImmutableValue<>(this.key, value);
    }

    @Override
    protected Value.Mutable<Vector3d> constructMutableValue(Vector3d actualValue) {
        return new SpongeMutableValue<>(this.key, actualValue);
    }

    @Override
    protected TargetedLocationData createManipulator() {
        return new SpongeTargetedLocationData();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected boolean supports(Entity entity) {
        return entity instanceof ITargetedLocation;
    }

}
