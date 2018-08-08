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
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableDamageableData;
import org.spongepowered.api.data.manipulator.mutable.entity.DamageableData;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeDamageableData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongeOptionalValue;

import java.util.Optional;

import javax.annotation.Nullable;

public class SpongeDamageableData extends AbstractData<DamageableData, ImmutableDamageableData> implements DamageableData {

    @Nullable private EntitySnapshot lastAttacker;
    @Nullable private Double lastDamage;

    public SpongeDamageableData() {
        this((EntitySnapshot) null, null);
    }

    public SpongeDamageableData(@Nullable EntitySnapshot lastAttacker, @Nullable Double lastDamage) {
        super(DamageableData.class);
        this.lastAttacker = lastAttacker;
        this.lastDamage = lastDamage;
        this.registerGettersAndSetters();
    }

    public SpongeDamageableData(@Nullable Living lastAttacker, @Nullable Double lastDamage) {
        this(lastAttacker == null ? null : lastAttacker.createSnapshot(), lastDamage);
    }

    @Override
    public OptionalValue<EntitySnapshot> lastAttacker() {
        return new SpongeOptionalValue<>(Keys.LAST_ATTACKER, Optional.empty(), Optional.ofNullable(this.lastAttacker));
    }

    @Override
    public OptionalValue<Double> lastDamage() {
        return new SpongeOptionalValue<>(Keys.LAST_DAMAGE, Optional.empty(), Optional.ofNullable(this.lastAttacker == null ? null : this.lastDamage));
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.LAST_ATTACKER, () -> Optional.ofNullable(this.lastAttacker));
        registerFieldSetter(Keys.LAST_ATTACKER, lastAttacker -> this.lastAttacker = lastAttacker == null ? null : lastAttacker.orElse(null));
        registerKeyValue(Keys.LAST_ATTACKER, this::lastAttacker);

        registerFieldGetter(Keys.LAST_DAMAGE, () -> Optional.ofNullable(this.lastDamage));
        registerFieldSetter(Keys.LAST_DAMAGE, lastDamage -> this.lastDamage = lastDamage == null ? null : lastDamage.orElse(null));
        registerKeyValue(Keys.LAST_DAMAGE, this::lastDamage);
    }

    @Override
    public DamageableData copy() {
        return new SpongeDamageableData(this.lastAttacker, this.lastDamage);
    }

    @Override
    public ImmutableDamageableData asImmutable() {
        return new ImmutableSpongeDamageableData(this.lastAttacker, this.lastDamage);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.LAST_ATTACKER, Optional.ofNullable(this.lastAttacker))
                .set(Keys.LAST_DAMAGE, Optional.ofNullable(this.lastDamage));
    }
}
