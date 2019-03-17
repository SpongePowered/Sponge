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
package org.spongepowered.common.data.manipulator.mutable.entity;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableTargetedEntityData;
import org.spongepowered.api.data.manipulator.mutable.entity.TargetedEntityData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeTargetedEntityData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.util.ImplementationRequiredForTest;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import javax.annotation.Nullable;

@ImplementationRequiredForTest
public final class SpongeTargetedEntityData extends AbstractData<TargetedEntityData, ImmutableTargetedEntityData>
        implements TargetedEntityData {

    @Nullable private EntitySnapshot value;

    public SpongeTargetedEntityData() {
        this(null);
    }

    public SpongeTargetedEntityData(EntitySnapshot value) {
        super(TargetedEntityData.class);
        this.value = value;
        registerGettersAndSetters();
    }

    @Override
    public TargetedEntityData copy() {
        return new SpongeTargetedEntityData(this.value);
    }

    @Override
    public ImmutableTargetedEntityData asImmutable() {
        return new ImmutableSpongeTargetedEntityData(this.value);
    }

    @Override
    public Value<EntitySnapshot> value() {
        return new SpongeValue<>(Keys.TARGETED_ENTITY, this.value);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.TARGETED_ENTITY, this.value);
    }

    public EntitySnapshot getValue() {
        return this.value;
    }

    public void setValue(EntitySnapshot value) {
        this.value = value;
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.TARGETED_ENTITY, SpongeTargetedEntityData.this::getValue);
        registerFieldSetter(Keys.TARGETED_ENTITY, SpongeTargetedEntityData.this::setValue);
        registerKeyValue(Keys.TARGETED_ENTITY, SpongeTargetedEntityData.this::value);
    }
}
