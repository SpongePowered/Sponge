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
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableDamageableData;
import org.spongepowered.api.data.manipulator.mutable.entity.DamageableData;
import org.spongepowered.api.data.value.immutable.ImmutableOptionalValue;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeDamageableData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeOptionalValue;

import java.util.Optional;

import javax.annotation.Nullable;

public class ImmutableSpongeDamageableData extends AbstractImmutableData<ImmutableDamageableData, DamageableData> implements ImmutableDamageableData {

    @Nullable private final EntitySnapshot lastAttacker;
    @Nullable private final Double lastDamage;

    private final ImmutableOptionalValue<EntitySnapshot> lastAttackerValue;
    private final ImmutableOptionalValue<Double> lastDamageValue;

    public ImmutableSpongeDamageableData() {
        this((EntitySnapshot) null, null);
    }

    public ImmutableSpongeDamageableData(@Nullable EntitySnapshot lastAttacker, @Nullable Double lastDamage) {
        super(ImmutableDamageableData.class);
        this.lastAttacker = lastAttacker;
        this.lastDamage = lastDamage;
        this.lastAttackerValue = new ImmutableSpongeOptionalValue<>(Keys.LAST_ATTACKER, Optional.ofNullable(this.lastAttacker));
        this.lastDamageValue = new ImmutableSpongeOptionalValue<>(Keys.LAST_DAMAGE, Optional.ofNullable(this.lastDamage));
        this.registerGetters();
    }

    public ImmutableSpongeDamageableData(@Nullable Living lastAttacker, @Nullable Double lastDamage) {
        this(lastAttacker == null ? null : lastAttacker.createSnapshot(), lastDamage);
    }

    @Override
    public ImmutableOptionalValue<EntitySnapshot> lastAttacker() {
        return this.lastAttackerValue;
    }

    @Override
    public ImmutableOptionalValue<Double> lastDamage() {
        return this.lastDamageValue;
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.LAST_ATTACKER, () -> Optional.ofNullable(this.lastAttacker));
        registerKeyValue(Keys.LAST_ATTACKER, () -> this.lastAttackerValue);

        registerFieldGetter(Keys.LAST_DAMAGE, () -> Optional.ofNullable(this.lastDamage));
        registerKeyValue(Keys.LAST_DAMAGE, () -> this.lastDamageValue);
    }

    @Override
    public DamageableData asMutable() {
        return new SpongeDamageableData(this.lastAttacker, this.lastDamage);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.LAST_ATTACKER, Optional.ofNullable(this.lastAttacker))
                .set(Keys.LAST_DAMAGE, Optional.ofNullable(this.lastDamage));
    }
}
