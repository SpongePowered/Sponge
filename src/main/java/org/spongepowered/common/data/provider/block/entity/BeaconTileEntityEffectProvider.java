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
package org.spongepowered.common.data.provider.block.entity;

import net.minecraft.potion.Effect;
import net.minecraft.tileentity.BeaconTileEntity;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.mixin.accessor.block.tileentity.BeaconTileEntityAccessor;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class BeaconTileEntityEffectProvider extends GenericMutableDataProvider<BeaconTileEntity, PotionEffectType> {

    private final Function<BeaconTileEntityAccessor, Effect> getter;
    private final BiConsumer<BeaconTileEntityAccessor, Effect> setter;

    BeaconTileEntityEffectProvider(Key<? extends Value<PotionEffectType>> key,
            Function<BeaconTileEntityAccessor, Effect> getter,
            BiConsumer<BeaconTileEntityAccessor, Effect> setter) {
        super(key);
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    protected Optional<PotionEffectType> getFrom(BeaconTileEntity dataHolder) {
        return Optional.ofNullable((PotionEffectType) this.getter.apply((BeaconTileEntityAccessor) dataHolder));
    }

    @Override
    protected boolean set(BeaconTileEntity dataHolder, PotionEffectType value) {
        final BeaconTileEntityAccessor accessor = (BeaconTileEntityAccessor) dataHolder;
        final Effect effect = (Effect) value;
        if (!BeaconTileEntityAccessor.getValidEffects().contains(effect)) {
            return false;
        }
        this.setter.accept(accessor, (Effect) value);
        dataHolder.markDirty();
        return true;
    }

    @Override
    protected boolean removeFrom(BeaconTileEntity dataHolder) {
        final BeaconTileEntityAccessor accessor = (BeaconTileEntityAccessor) dataHolder;
        if (this.getter.apply(accessor) != null) {
            this.setter.accept(accessor, null);
            dataHolder.markDirty();
        }
        return true;
    }
}
