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

import com.google.common.base.Optional;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableTameableData;
import org.spongepowered.api.data.manipulator.mutable.entity.TameableData;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeTameableData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongeOptionalValue;
import org.spongepowered.common.util.GetterFunction;
import org.spongepowered.common.util.SetterFunction;

import javax.annotation.Nullable;
import java.util.UUID;

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
        return new SpongeOptionalValue<UUID>(Keys.TAMED_OWNER, Optional.fromNullable(owner));
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
    public int compareTo(TameableData o) {
        return ComparisonChain.start()
                .compare(owner, o.owner().get().orNull(), Ordering.natural().nullsFirst())
                .result();
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                //TODO MemoryDataContainer will null check this.owner
                .set(Keys.TAMED_OWNER.getQuery(), this.owner);
    }

    public Optional<UUID> getOwner(){
        return Optional.fromNullable(owner);
    }

    public SpongeTameableData setOwner(@Nullable UUID owner){
        //No null checking or copying required, UUID is final & immutable.
        this.owner = owner;
        return this;
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.TAMED_OWNER, new GetterFunction<Object>() {
            @Override
            public Object get() {
                return owner();
            }
        });
        registerFieldSetter(Keys.TAMED_OWNER, new SetterFunction<Object>() {
            @Override
            public void set(Object value) {
                setOwner((UUID) value);
            }
        });
        registerKeyValue(Keys.TAMED_OWNER, new GetterFunction<Value<?>>() {
            @Override
            public Value<?> get() {
                return owner();
            }
        });
    }
}
