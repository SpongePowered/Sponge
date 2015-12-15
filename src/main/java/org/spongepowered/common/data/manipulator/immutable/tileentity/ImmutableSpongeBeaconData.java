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

import com.google.common.collect.ComparisonChain;
import net.minecraft.potion.Potion;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableBeaconData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.BeaconData;
import org.spongepowered.api.data.value.immutable.ImmutableOptionalValue;
import org.spongepowered.api.potion.PotionEffectType;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeBeaconData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeOptionalValue;

import java.util.Optional;

public class ImmutableSpongeBeaconData extends AbstractImmutableData<ImmutableBeaconData, BeaconData> implements ImmutableBeaconData {

    private final Optional<PotionEffectType> primaryEffect;
    private final ImmutableSpongeOptionalValue<PotionEffectType> primaryEffectsValue;
    private final Optional<PotionEffectType> secondaryEffect;
    private final ImmutableSpongeOptionalValue<PotionEffectType> secondaryEffectsValue;

    public ImmutableSpongeBeaconData(Optional<PotionEffectType> primaryEffect, Optional<PotionEffectType> secondaryEffect) {
        super(ImmutableBeaconData.class);
        this.primaryEffect = primaryEffect;
        this.primaryEffectsValue = new ImmutableSpongeOptionalValue<>(Keys.BEACON_PRIMARY_EFFECT, this.primaryEffect);
        this.secondaryEffect = secondaryEffect;
        this.secondaryEffectsValue = new ImmutableSpongeOptionalValue<>(Keys.BEACON_SECONDARY_EFFECT, this.secondaryEffect);
        this.registerGetters();
    }

    @Override
    protected void registerGetters() {
        this.registerFieldGetter(Keys.BEACON_PRIMARY_EFFECT, ImmutableSpongeBeaconData.this::getPrimaryEffect);
        this.registerKeyValue(Keys.BEACON_PRIMARY_EFFECT, ImmutableSpongeBeaconData.this::primaryEffect);

        this.registerFieldGetter(Keys.BEACON_SECONDARY_EFFECT, ImmutableSpongeBeaconData.this::getSecondaryEffect);
        this.registerKeyValue(Keys.BEACON_SECONDARY_EFFECT, ImmutableSpongeBeaconData.this::secondaryEffect);
    }

    @Override
    public ImmutableOptionalValue<PotionEffectType> primaryEffect() {
        return this.primaryEffectsValue;
    }

    @Override
    public ImmutableOptionalValue<PotionEffectType> secondaryEffect() {
        return this.secondaryEffectsValue;
    }

    @Override
    public ImmutableBeaconData clearEffects() {
        return new ImmutableSpongeBeaconData(Optional.empty(), Optional.empty());
    }

    @Override
    public BeaconData asMutable() {
        return new SpongeBeaconData(this.primaryEffect, this.secondaryEffect);
    }

    @Override
    public int compareTo(ImmutableBeaconData o) {
        return ComparisonChain.start()
                .compare(((Potion) o.primaryEffect().get().get()).getId(), ((Potion) this.primaryEffect.get()).getId())
                .compare(((Potion) o.secondaryEffect().get().get()).getId(), ((Potion) this.secondaryEffect.get()).getId())
                .result();
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

    public Optional<PotionEffectType> getSecondaryEffect() {
        return this.secondaryEffect;
    }
}
