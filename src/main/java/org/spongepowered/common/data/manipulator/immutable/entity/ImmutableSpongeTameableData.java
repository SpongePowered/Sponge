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

import com.google.common.base.Optional;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableTameableData;
import org.spongepowered.api.data.manipulator.mutable.entity.TameableData;
import org.spongepowered.api.data.value.immutable.ImmutableOptionalValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeTameableData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeOptionalValue;
import org.spongepowered.common.util.GetterFunction;

import javax.annotation.Nullable;
import java.util.UUID;

public class ImmutableSpongeTameableData extends AbstractImmutableData<ImmutableTameableData, TameableData> implements ImmutableTameableData {

    @Nullable private final UUID owner;
    @Nullable private static ImmutableSpongeTameableData empty = null;
    @Nullable private static ImmutableSpongeOptionalValue<UUID> emptyValue = null;

    private ImmutableSpongeTameableData(@Nullable UUID owner, Class clazz) {
        super(ImmutableTameableData.class);
        this.owner = owner;
    }

    @Override
    public ImmutableOptionalValue<UUID> owner() {
        return createValue(owner);
    }

    @Override
    public ImmutableTameableData copy() {
        return this; //It's immutable, just return this, UUID is also immutable.
    }

    @Override
    public TameableData asMutable() {
        return new SpongeTameableData(owner);
    }

    @Override
    public int compareTo(ImmutableTameableData o) {
        return ComparisonChain.start()
                //Must use a nullsFirst or nullsLast or else ComparisonChain throws an NPE.
                .compare(owner, o.owner().get().orNull(), Ordering.natural().nullsFirst())
                .result();
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                //TODO: .set will checkNotNull this.owner.
                .set(Keys.TAMED_OWNER.getQuery(), this.owner);
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.TAMED_OWNER, new GetterFunction<Object>() {
            @Override
            public Object get() {
                return getOwner();
            }
        });
        registerKeyValue(Keys.TAMED_OWNER, new GetterFunction<ImmutableValue<?>>() {
            @Override
            public ImmutableValue<?> get() {
                return owner();
            }
        });
    }

    public Optional<UUID> getOwner() {
        return Optional.fromNullable(this.owner);
    }

    private static ImmutableSpongeTameableData getEmpty(){
        if(empty == null) {
            empty = new ImmutableSpongeTameableData(null);
        }
        return empty;
    }

    private static ImmutableSpongeOptionalValue<UUID> getEmptyValue() {
        if(emptyValue == null) {
            emptyValue = new ImmutableSpongeOptionalValue<UUID>(Keys.TAMED_OWNER, Optional.<UUID>absent());
        }
        return emptyValue;
    }

    public static ImmutableTameableData create(@Nullable UUID owner) {
        if(owner == null) {
            return getEmpty();
        } else {
            return new ImmutableSpongeTameableData(owner);
        }
    }

    public ImmutableOptionalValue<UUID> createValue(@Nullable UUID owner) {
        if(owner == null) {
            return getEmptyValue();
        } else {
            return createValue(Optional.of(owner));
        }
    }

    public static ImmutableSpongeOptionalValue<UUID> createValue(Optional<UUID> owner) {
        if(!owner.isPresent()) {
            return getEmptyValue();
        } else {
            return new ImmutableSpongeOptionalValue<UUID>(Keys.TAMED_OWNER, owner);
        }
    }
}
