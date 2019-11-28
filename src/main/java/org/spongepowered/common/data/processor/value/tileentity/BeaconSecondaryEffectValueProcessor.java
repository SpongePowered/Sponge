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
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.OptionalValue;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.bridge.tileentity.TileEntityBeaconBridge;

import java.util.Optional;
import net.minecraft.potion.Effect;
import net.minecraft.tileentity.BeaconTileEntity;

public class BeaconSecondaryEffectValueProcessor
        extends AbstractSpongeValueProcessor<BeaconTileEntity, Optional<PotionEffectType>, OptionalValue<PotionEffectType>> {

    public BeaconSecondaryEffectValueProcessor() {
        super(BeaconTileEntity.class, Keys.BEACON_SECONDARY_EFFECT);
    }

    @Override
    protected OptionalValue<PotionEffectType> constructValue(Optional<PotionEffectType> actualValue) {
        return SpongeValueFactory.getInstance().createOptionalValue(Keys.BEACON_SECONDARY_EFFECT, actualValue.orElse(null));
    }

    @Override
    protected boolean set(BeaconTileEntity container, Optional<PotionEffectType> value) {
        if (container.getField(0) != 4) {
            return false;
        }
        ((TileEntityBeaconBridge) container).bridge$forceSetSecondaryEffect((Effect) value.orElse(null));
        container.markDirty();
        return true;
    }

    @Override
    protected Optional<Optional<PotionEffectType>> getVal(BeaconTileEntity container) {
        if (container.getField(0) != 4) {
            return Optional.of(Optional.empty());
        }
        int id = container.getField(2);
        if (id > 0) {
            return Optional.of(Optional.of((PotionEffectType) Effect.get(id)));
        }
        return Optional.of(Optional.empty());
    }

    @Override
    protected ImmutableValue<Optional<PotionEffectType>> constructImmutableValue(Optional<PotionEffectType> value) {
        return constructValue(value).asImmutable();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
