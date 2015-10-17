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
package org.spongepowered.common.data.processor.value.tileentity;

import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntityBeacon;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.api.potion.PotionEffectType;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeOptionalValue;
import org.spongepowered.common.interfaces.tileentity.IMixinTileEntityBeacon;

import java.util.Optional;

public class BeaconPrimaryEffectValueProcessor extends AbstractSpongeValueProcessor<TileEntityBeacon, Optional<PotionEffectType>, OptionalValue<PotionEffectType>> {

    public BeaconPrimaryEffectValueProcessor() {
        super(TileEntityBeacon.class, Keys.BEACON_PRIMARY_EFFECT);
    }

    @Override
    protected OptionalValue<PotionEffectType> constructValue(Optional<PotionEffectType> defaultValue) {
        return new SpongeOptionalValue<>(Keys.BEACON_PRIMARY_EFFECT, defaultValue);
    }

    @Override
    protected boolean set(TileEntityBeacon container, Optional<PotionEffectType> value) {
        return false;
    }

    @Override
    protected Optional<Optional<PotionEffectType>> getVal(TileEntityBeacon container) {
        return Optional.of(Optional.of((PotionEffectType) Potion.potionTypes[((IMixinTileEntityBeacon) container).getPrimaryEffect()]));
    }

    @Override
    protected ImmutableValue<Optional<PotionEffectType>> constructImmutableValue(Optional<PotionEffectType> value) {
        return new ImmutableSpongeValue<>(Keys.BEACON_PRIMARY_EFFECT, value);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return null;
    }
}
