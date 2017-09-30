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
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableTameableData;
import org.spongepowered.api.data.manipulator.mutable.entity.TameableData;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeTameableData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongeOptionalValue;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

public class SpongeTameableData extends AbstractData<TameableData, ImmutableTameableData> implements TameableData  {

    @Nullable private UUID owner;

    public SpongeTameableData(@Nullable UUID owner) {
        super(TameableData.class);
        this.owner = owner;
        registerGettersAndSetters();
    }

    public SpongeTameableData() {
        this(null);
    }

    @Override
    public OptionalValue<UUID> owner() {
        return new SpongeOptionalValue<>(Keys.TAMED_OWNER, Optional.ofNullable(this.owner));
    }

    @Override
    public TameableData copy() {
        return new SpongeTameableData(this.owner);
    }

    @Override
    public ImmutableTameableData asImmutable() {
        return ImmutableSpongeTameableData.create(this.owner);
    }

    @Override
    public DataContainer toContainer() {
        final String uuid = this.owner == null ? "none" : this.owner.toString();
        return super.toContainer()
                .set(Keys.TAMED_OWNER.getQuery(), uuid);
    }

    public Optional<UUID> getOwner(){
        return Optional.ofNullable(this.owner);
    }

    public SpongeTameableData setOwner(Optional<UUID> owner) {
        //No null checking or copying required, UUID is final & immutable.
        this.owner = owner.orElse(null);
        return this;
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.TAMED_OWNER, SpongeTameableData.this::getOwner);
        registerFieldSetter(Keys.TAMED_OWNER, SpongeTameableData.this::setOwner);
        registerKeyValue(Keys.TAMED_OWNER, SpongeTameableData.this::owner);
    }
}
