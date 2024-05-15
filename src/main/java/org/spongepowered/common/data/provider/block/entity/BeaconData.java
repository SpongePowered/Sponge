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

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.common.accessor.world.level.block.entity.BeaconBlockEntityAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

import java.util.function.BiConsumer;
import java.util.function.Function;

public final class BeaconData {

    private BeaconData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(BeaconBlockEntity.class)
                    .create(Keys.PRIMARY_POTION_EFFECT_TYPE)
                        .get(h -> BeaconData.get(h, BeaconBlockEntityAccessor::accessor$primaryPower))
                        .setAnd((h, v) -> BeaconData.set(h, v, BeaconBlockEntityAccessor::accessor$primaryPower))
                        .deleteAnd(h -> BeaconData.delete(h, BeaconBlockEntityAccessor::accessor$primaryPower,
                                BeaconBlockEntityAccessor::accessor$primaryPower))
                    .create(Keys.SECONDARY_POTION_EFFECT_TYPE)
                        .get(h -> BeaconData.get(h, BeaconBlockEntityAccessor::accessor$secondaryPower))
                        .setAnd((h, v) -> BeaconData.set(h, v, BeaconBlockEntityAccessor::accessor$secondaryPower))
                        .deleteAnd(h -> BeaconData.delete(h, BeaconBlockEntityAccessor::accessor$secondaryPower,
                                BeaconBlockEntityAccessor::accessor$secondaryPower));
    }
    // @formatter:on

    private static PotionEffectType get(final BeaconBlockEntity holder, final Function<BeaconBlockEntityAccessor, Holder<MobEffect>> getter) {
        return (PotionEffectType) getter.apply((BeaconBlockEntityAccessor) holder).value();
    }

    private static boolean set(final BeaconBlockEntity holder, final PotionEffectType value,
            final BiConsumer<BeaconBlockEntityAccessor, Holder<MobEffect>> setter) {
        final BeaconBlockEntityAccessor accessor = (BeaconBlockEntityAccessor) holder;
        final MobEffect effect = (MobEffect) value;
        if (!BeaconBlockEntityAccessor.accessor$VALID_EFFECTS().contains(effect)) {
            return false;
        }
        setter.accept(accessor, Holder.direct((MobEffect) value));
        holder.setChanged();
        return true;
    }

    private static boolean delete(final BeaconBlockEntity holder, final Function<BeaconBlockEntityAccessor, Holder<MobEffect>> getter,
            final BiConsumer<BeaconBlockEntityAccessor, Holder<MobEffect>> setter) {
        final BeaconBlockEntityAccessor accessor = (BeaconBlockEntityAccessor) holder;
        if (accessor.accessor$primaryPower() != null) {
            setter.accept(accessor, null);
            holder.setChanged();
        }
        return true;
    }
}
