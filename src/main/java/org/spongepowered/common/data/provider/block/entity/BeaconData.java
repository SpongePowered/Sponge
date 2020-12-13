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
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.common.accessor.tileentity.BeaconTileEntityAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

import java.util.function.BiConsumer;
import java.util.function.Function;

public final class BeaconData {

    private BeaconData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(BeaconTileEntity.class)
                    .create(Keys.PRIMARY_POTION_EFFECT_TYPE)
                        .get(h -> BeaconData.get(h, BeaconTileEntityAccessor::accessor$getPrimaryEffect))
                        .setAnd((h, v) -> BeaconData.set(h, v, BeaconTileEntityAccessor::accessor$setPrimaryEffect))
                        .deleteAnd(h -> BeaconData.delete(h, BeaconTileEntityAccessor::accessor$getPrimaryEffect,
                                BeaconTileEntityAccessor::accessor$setPrimaryEffect))
                    .create(Keys.SECONDARY_POTION_EFFECT_TYPE)
                        .get(h -> BeaconData.get(h, BeaconTileEntityAccessor::accessor$getSecondaryEffect))
                        .setAnd((h, v) -> BeaconData.set(h, v, BeaconTileEntityAccessor::accessor$setSecondaryEffect))
                        .deleteAnd(h -> BeaconData.delete(h, BeaconTileEntityAccessor::accessor$getSecondaryEffect,
                                BeaconTileEntityAccessor::accessor$setSecondaryEffect));
    }
    // @formatter:on

    private static PotionEffectType get(final BeaconTileEntity holder, final Function<BeaconTileEntityAccessor, Effect> getter) {
        return (PotionEffectType) getter.apply((BeaconTileEntityAccessor) holder);
    }

    private static boolean set(final BeaconTileEntity holder, final PotionEffectType value,
            final BiConsumer<BeaconTileEntityAccessor, Effect> setter) {
        final BeaconTileEntityAccessor accessor = (BeaconTileEntityAccessor) holder;
        final Effect effect = (Effect) value;
        if (!BeaconTileEntityAccessor.getValidEffects().contains(effect)) {
            return false;
        }
        setter.accept(accessor, (Effect) value);
        holder.markDirty();
        return true;
    }

    private static boolean delete(final BeaconTileEntity holder, final Function<BeaconTileEntityAccessor, Effect> getter,
            final BiConsumer<BeaconTileEntityAccessor, Effect> setter) {
        final BeaconTileEntityAccessor accessor = (BeaconTileEntityAccessor) holder;
        if (accessor.accessor$getPrimaryEffect() != null) {
            setter.accept(accessor, null);
            holder.markDirty();
        }
        return true;
    }
}
