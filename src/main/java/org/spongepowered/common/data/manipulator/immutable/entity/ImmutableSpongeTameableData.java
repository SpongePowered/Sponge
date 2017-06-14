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

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableTameableData;
import org.spongepowered.api.data.manipulator.mutable.entity.TameableData;
import org.spongepowered.api.data.value.immutable.ImmutableOptionalValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeTameableData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeOptionalValue;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

public class ImmutableSpongeTameableData extends AbstractImmutableData<ImmutableTameableData, TameableData> implements ImmutableTameableData {

    private static final ImmutableSpongeOptionalValue<UUID> EMPTY_VALUE = new ImmutableSpongeOptionalValue<>(Keys.TAMED_OWNER, Optional.empty());
    private static final ImmutableSpongeTameableData EMPTY_DATA = new ImmutableSpongeTameableData(null);

    @Nullable private final UUID owner;
    private final ImmutableOptionalValue<UUID> immutableValue;

    public ImmutableSpongeTameableData(@Nullable UUID owner) {
        super(ImmutableTameableData.class);
        this.owner = owner;
        if (this.owner == null) {
            this.immutableValue = EMPTY_VALUE;
        } else {
            this.immutableValue = new ImmutableSpongeOptionalValue<>(Keys.TAMED_OWNER, Optional.of(this.owner));
        }
        registerGetters();
    }

    @Override
    public ImmutableOptionalValue<UUID> owner() {
        return this.immutableValue;
    }

    @Override
    public TameableData asMutable() {
        return new SpongeTameableData(this.owner);
    }

    @Override
    public DataContainer toContainer() {
        final String owner = this.owner == null ? "none" : this.owner.toString();
        return super.toContainer()
            .set(Keys.TAMED_OWNER.getQuery(), owner);
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.TAMED_OWNER, ImmutableSpongeTameableData.this::getOwner);
        registerKeyValue(Keys.TAMED_OWNER, ImmutableSpongeTameableData.this::owner);
    }

    public Optional<UUID> getOwner() {
        return Optional.ofNullable(this.owner);
    }

    public static ImmutableTameableData create(@Nullable UUID owner) {
        if (owner == null) {
            return EMPTY_DATA;
        }
        return new ImmutableSpongeTameableData(owner);
    }

    public static ImmutableSpongeOptionalValue<UUID> createValue(Optional<UUID> owner) {
        if (!owner.isPresent()) {
            return EMPTY_VALUE;
        }
        return new ImmutableSpongeOptionalValue<>(Keys.TAMED_OWNER, owner);
    }
}
