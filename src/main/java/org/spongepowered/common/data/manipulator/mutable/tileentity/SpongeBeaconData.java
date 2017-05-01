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
package org.spongepowered.common.data.manipulator.mutable.tileentity;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableBeaconData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.BeaconData;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.common.data.manipulator.immutable.tileentity.ImmutableSpongeBeaconData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongeOptionalValue;

import java.util.Optional;

import javax.annotation.Nullable;

public class SpongeBeaconData extends AbstractData<BeaconData, ImmutableBeaconData> implements BeaconData {

    @Nullable
    private PotionEffectType primaryEffect;
    @Nullable
    private PotionEffectType secondaryEffect;

    public SpongeBeaconData() {
        this(null, null);
    }

    public SpongeBeaconData(@Nullable PotionEffectType primaryEffect, @Nullable PotionEffectType secondaryEffect) {
        super(BeaconData.class);
        this.primaryEffect = primaryEffect;
        this.secondaryEffect = secondaryEffect;
        registerGettersAndSetters();
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.BEACON_PRIMARY_EFFECT, this::getPrimaryEffect);
        registerFieldSetter(Keys.BEACON_PRIMARY_EFFECT, this::setPrimaryEffect);
        registerKeyValue(Keys.BEACON_PRIMARY_EFFECT, this::primaryEffect);

        registerFieldGetter(Keys.BEACON_SECONDARY_EFFECT, this::getSecondaryEffect);
        registerFieldSetter(Keys.BEACON_SECONDARY_EFFECT, this::setSecondaryEffect);
        registerKeyValue(Keys.BEACON_SECONDARY_EFFECT, this::secondaryEffect);
    }

    public Optional<PotionEffectType> getPrimaryEffect() {
        return Optional.ofNullable(this.primaryEffect);
    }

    public void setPrimaryEffect(Optional<PotionEffectType> primaryEffect) {
        this.primaryEffect = primaryEffect.orElse(null);
    }

    @Override
    public OptionalValue<PotionEffectType> primaryEffect() {
        return new SpongeOptionalValue<>(Keys.BEACON_PRIMARY_EFFECT, Optional.ofNullable(this.primaryEffect));
    }

    public Optional<PotionEffectType> getSecondaryEffect() {
        return Optional.ofNullable(this.secondaryEffect);
    }

    public void setSecondaryEffect(Optional<PotionEffectType> secondaryEffect) {
        this.secondaryEffect = secondaryEffect.orElse(null);
    }

    @Override
    public OptionalValue<PotionEffectType> secondaryEffect() {
        return new SpongeOptionalValue<>(Keys.BEACON_SECONDARY_EFFECT, Optional.ofNullable(this.secondaryEffect));
    }

    @Override
    public BeaconData clearEffects() {
        this.primaryEffect = null;
        this.secondaryEffect = null;
        return this;
    }

    @Override
    public BeaconData copy() {
        return new SpongeBeaconData(this.primaryEffect, this.secondaryEffect);
    }

    @Override
    public ImmutableBeaconData asImmutable() {
        return new ImmutableSpongeBeaconData(this.primaryEffect, this.secondaryEffect);
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
