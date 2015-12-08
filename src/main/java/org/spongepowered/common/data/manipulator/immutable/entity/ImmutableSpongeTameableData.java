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

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
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

    //TODO: Wat

    //Lazily initialized, do not use directly, use create & createValue factory methods instead.
    @Nullable private static ImmutableSpongeTameableData empty = null;
    @Nullable private static ImmutableSpongeOptionalValue<UUID> emptyValue = null;

    @Nullable private final UUID owner;

    public ImmutableSpongeTameableData(@Nullable UUID owner) {
        super(ImmutableTameableData.class);
        this.owner = owner;
        registerGetters();
    }

    @Override
    public ImmutableOptionalValue<UUID> owner() {
        return createValue(this.owner);
    }

    @Override
    public TameableData asMutable() {
        return new SpongeTameableData(this.owner);
    }

    @Override
    public int compareTo(ImmutableTameableData o) {
        return ComparisonChain.start()
            .compare(this.owner, o.owner().get().orElse(null), Ordering.natural().nullsFirst())
            .result();
    }

    @Override
    public DataContainer toContainer() {
        final String owner = this.owner == null ? "none" : this.owner.toString();
        return new MemoryDataContainer()
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

    private static ImmutableSpongeTameableData getEmpty() {
        if (empty == null) {
            empty = new ImmutableSpongeTameableData(null);
        }
        return empty;
    }

    private static ImmutableSpongeOptionalValue<UUID> getEmptyValue() {
        if (emptyValue == null) {
            emptyValue = new ImmutableSpongeOptionalValue<>(Keys.TAMED_OWNER, Optional.empty());
        }
        return emptyValue;
    }

    public static ImmutableTameableData create(@Nullable UUID owner) {
        if (owner == null) {
            return getEmpty();
        } else {
            return new ImmutableSpongeTameableData(owner);
        }
    }

    public static ImmutableOptionalValue<UUID> createValue(@Nullable UUID owner) {
        if (owner == null) {
            return getEmptyValue();
        } else {
            return createValue(Optional.of(owner));
        }
    }

    public static ImmutableSpongeOptionalValue<UUID> createValue(Optional<UUID> owner) {
        if (!owner.isPresent()) {
            return getEmptyValue();
        } else {
            return new ImmutableSpongeOptionalValue<>(Keys.TAMED_OWNER, owner);
        }
    }
}
