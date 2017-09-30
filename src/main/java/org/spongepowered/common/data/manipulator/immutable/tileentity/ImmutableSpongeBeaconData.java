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
package org.spongepowered.common.data.manipulator.immutable.tileentity;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableBeaconData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.BeaconData;
import org.spongepowered.api.data.value.immutable.ImmutableOptionalValue;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeBeaconData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeOptionalValue;

import java.util.Optional;

import javax.annotation.Nullable;

public class ImmutableSpongeBeaconData extends AbstractImmutableData<ImmutableBeaconData, BeaconData> implements ImmutableBeaconData {

    @Nullable
    private final PotionEffectType primaryEffect;
    @Nullable
    private final PotionEffectType secondaryEffect;
    private final ImmutableOptionalValue<PotionEffectType> primaryEffectValue;
    private final ImmutableOptionalValue<PotionEffectType> secondaryEffectValue;

    public ImmutableSpongeBeaconData(@Nullable PotionEffectType primaryEffect, @Nullable PotionEffectType secondaryEffect) {
        super(ImmutableBeaconData.class);
        this.primaryEffect = primaryEffect;
        this.secondaryEffect = secondaryEffect;
        this.primaryEffectValue = new ImmutableSpongeOptionalValue<>(Keys.BEACON_PRIMARY_EFFECT, Optional.ofNullable(this.primaryEffect));
        this.secondaryEffectValue = new ImmutableSpongeOptionalValue<>(Keys.BEACON_SECONDARY_EFFECT, Optional.ofNullable(this.secondaryEffect));
        registerGetters();
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.BEACON_PRIMARY_EFFECT, this::getPrimaryEffect);
        registerKeyValue(Keys.BEACON_PRIMARY_EFFECT, this::primaryEffect);

        registerFieldGetter(Keys.BEACON_SECONDARY_EFFECT, this::getSecondaryEffect);
        registerKeyValue(Keys.BEACON_SECONDARY_EFFECT, this::secondaryEffect);
    }

    public Optional<PotionEffectType> getPrimaryEffect() {
        return Optional.ofNullable(this.primaryEffect);
    }

    @Override
    public ImmutableOptionalValue<PotionEffectType> primaryEffect() {
        return this.primaryEffectValue;
    }

    public Optional<PotionEffectType> getSecondaryEffect() {
        return Optional.ofNullable(this.secondaryEffect);
    }

    @Override
    public ImmutableOptionalValue<PotionEffectType> secondaryEffect() {
        return this.secondaryEffectValue;
    }

    @Override
    public ImmutableBeaconData clearEffects() {
        return new ImmutableSpongeBeaconData(null, null);
    }

    @Override
    public BeaconData asMutable() {
        return new SpongeBeaconData(this.primaryEffect, this.secondaryEffect);
    }

    @Override
    public DataContainer toContainer() {
        DataContainer dataContainer = super.toContainer();
        if (this.primaryEffect != null) {
            dataContainer = dataContainer.set(Keys.BEACON_PRIMARY_EFFECT.getQuery(), this.primaryEffect.getId());
        }
        if (this.secondaryEffect != null) {
            dataContainer = dataContainer.set(Keys.BEACON_SECONDARY_EFFECT.getQuery(), this.secondaryEffect.getId());
        }
        return dataContainer;
    }

}
