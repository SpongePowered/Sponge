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
package org.spongepowered.common.data.manipulator.immutable.entity;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableTargetedEntityData;
import org.spongepowered.api.data.manipulator.mutable.entity.TargetedEntityData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeTargetedEntityData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

import javax.annotation.Nullable;

public final class ImmutableSpongeTargetedEntityData extends AbstractImmutableSingleData<EntitySnapshot, ImmutableTargetedEntityData, TargetedEntityData>
        implements ImmutableTargetedEntityData {

    private final ImmutableValue<EntitySnapshot> immutableValue;

    public ImmutableSpongeTargetedEntityData(@Nullable EntitySnapshot value) {
        super(ImmutableTargetedEntityData.class, value, Keys.TARGETED_ENTITY);
        this.immutableValue = new ImmutableSpongeValue<>(this.usedKey, value);
    }

    @Override
    protected ImmutableValue<EntitySnapshot> getValueGetter() {
        return this.value();
    }

    @Override
    public TargetedEntityData asMutable() {
        return new SpongeTargetedEntityData(this.value);
    }

    @Override
    public ImmutableValue<EntitySnapshot> value() {
        return this.immutableValue;
    }
}
