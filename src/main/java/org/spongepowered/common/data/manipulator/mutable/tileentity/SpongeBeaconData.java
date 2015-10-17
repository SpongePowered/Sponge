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
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableBeaconData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.BeaconData;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.api.potion.PotionEffectType;
import org.spongepowered.common.data.manipulator.immutable.tileentity.ImmutableSpongeBeaconData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongeOptionalValue;

import java.util.Optional;

public class SpongeBeaconData extends AbstractData<BeaconData, ImmutableBeaconData> implements BeaconData {

    private Optional<PotionEffectType> primaryEffect;
    private Optional<PotionEffectType> secondaryEffect;

    public SpongeBeaconData(Optional<PotionEffectType> primaryEffect, Optional<PotionEffectType> secondaryEffect) {
        super(BeaconData.class);
        this.registerGettersAndSetters();
        this.primaryEffect = primaryEffect;
        this.secondaryEffect = secondaryEffect;
    }

    public SpongeBeaconData() {
        this(Optional.empty(), Optional.empty());
    }

    @Override
    protected void registerGettersAndSetters() {
        this.registerFieldGetter(Keys.BEACON_PRIMARY_EFFECT, SpongeBeaconData.this::getPrimaryEffect);
        this.registerFieldSetter(Keys.BEACON_PRIMARY_EFFECT, SpongeBeaconData.this::setPrimaryEffect);
        this.registerKeyValue(Keys.BEACON_PRIMARY_EFFECT, SpongeBeaconData.this::primaryEffect);

        this.registerFieldGetter(Keys.BEACON_SECONDARY_EFFECT, SpongeBeaconData.this::getSecondaryEffect);
        this.registerFieldSetter(Keys.BEACON_SECONDARY_EFFECT, SpongeBeaconData.this::setSecondaryEffect);
        this.registerKeyValue(Keys.BEACON_SECONDARY_EFFECT, SpongeBeaconData.this::secondaryEffect);
    }

    @Override
    public OptionalValue<PotionEffectType> primaryEffect() {
        return new SpongeOptionalValue<>(Keys.BEACON_PRIMARY_EFFECT, this.getPrimaryEffect());
    }

    @Override
    public OptionalValue<PotionEffectType> secondaryEffect() {
        return new SpongeOptionalValue<>(Keys.BEACON_SECONDARY_EFFECT, this.getSecondaryEffect());
    }

    @Override
    public BeaconData clearEffects() {
        this.primaryEffect = Optional.empty();
        this.secondaryEffect = Optional.empty();
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
    public int compareTo(BeaconData o) {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(Keys.BEACON_PRIMARY_EFFECT.getQuery(), this.primaryEffect)
                .set(Keys.BEACON_SECONDARY_EFFECT.getQuery(), this.secondaryEffect);
    }

    public Optional<PotionEffectType> getPrimaryEffect() {
        return this.primaryEffect;
    }

    public void setPrimaryEffect(Optional<PotionEffectType> effectType) {
        this.primaryEffect = effectType;
    }

    public Optional<PotionEffectType> getSecondaryEffect() {
        return this.secondaryEffect;
    }

    public void setSecondaryEffect(Optional<PotionEffectType> effectType) {
        this.secondaryEffect = effectType;
    }
}
