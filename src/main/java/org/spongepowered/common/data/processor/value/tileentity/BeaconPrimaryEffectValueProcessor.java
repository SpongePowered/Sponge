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

import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.OptionalValue.Mutable;
import org.spongepowered.api.data.value.Value.Immutable;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.bridge.tileentity.BeaconTileEntityBridge;

import java.util.Optional;
import net.minecraft.potion.Effect;
import net.minecraft.tileentity.BeaconTileEntity;

public class BeaconPrimaryEffectValueProcessor
        extends AbstractSpongeValueProcessor<BeaconTileEntity, Optional<PotionEffectType>, Mutable<PotionEffectType>> {

    public BeaconPrimaryEffectValueProcessor() {
        super(BeaconTileEntity.class, Keys.BEACON_PRIMARY_EFFECT);
    }

    @Override
    protected Mutable<PotionEffectType> constructValue(Optional<PotionEffectType> actualValue) {
        return SpongeValueFactory.getInstance().createOptionalValue(Keys.BEACON_PRIMARY_EFFECT, actualValue.orElse(null));
    }

    @Override
    protected boolean set(BeaconTileEntity container, Optional<PotionEffectType> value) {
        ((BeaconTileEntityBridge) container).bridge$forceSetPrimaryEffect((Effect) value.orElse(null));
        container.markDirty();
        return true;
    }

    @Override
    protected Optional<Optional<PotionEffectType>> getVal(BeaconTileEntity container) {
        int id = container.getField(1);
        if (id > 0) {
            return Optional.of(Optional.of((PotionEffectType) Effect.get(id)));
        }
        return Optional.of(Optional.empty());
    }

    @Override
    protected Immutable<Optional<PotionEffectType>> constructImmutableValue(Optional<PotionEffectType> value) {
        return this.constructValue(value).asImmutable();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
