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
package org.spongepowered.common.data.processor.multi.tileentity;

import com.google.common.collect.ImmutableMap;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntityBeacon;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableBeaconData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.BeaconData;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeBeaconData;
import org.spongepowered.common.data.processor.common.AbstractTileEntityDataProcessor;
import org.spongepowered.common.bridge.tileentity.TileEntityBeaconBridge;

import java.util.Map;
import java.util.Optional;

public class BeaconDataProcessor extends AbstractTileEntityDataProcessor<TileEntityBeacon, BeaconData, ImmutableBeaconData> {

    public BeaconDataProcessor() {
        super(TileEntityBeacon.class);
    }

    @Override
    protected boolean doesDataExist(TileEntityBeacon dataHolder) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean set(TileEntityBeacon dataHolder, Map<Key<?>, Object> keyValues) {
        Potion primary = ((Optional<Potion>) keyValues.get(Keys.BEACON_PRIMARY_EFFECT)).orElse(null);
        Potion secondary = ((Optional<Potion>) keyValues.get(Keys.BEACON_SECONDARY_EFFECT)).orElse(null);

        ((TileEntityBeaconBridge) dataHolder).bridge$forceSetPrimaryEffect(primary);
        ((TileEntityBeaconBridge) dataHolder).bridge$forceSetSecondaryEffect(secondary);

        dataHolder.func_70296_d();
        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(TileEntityBeacon dataHolder) {
        ImmutableMap.Builder<Key<?>, Object> builder = ImmutableMap.builder();
        int primaryID = dataHolder.func_174887_a_(1);
        int secondaryID = dataHolder.func_174887_a_(2);
        if (primaryID > 0) {
            builder.put(Keys.BEACON_PRIMARY_EFFECT, Optional.ofNullable(Potion.func_188412_a(primaryID)));
        }
        if (secondaryID > 0 && dataHolder.func_174887_a_(0) == 4) {
            builder.put(Keys.BEACON_SECONDARY_EFFECT, Optional.ofNullable(Potion.func_188412_a(secondaryID)));
        }
        return builder.build();
    }

    @Override
    protected BeaconData createManipulator() {
        return new SpongeBeaconData();
    }

    @Override
    public Optional<BeaconData> fill(DataContainer container, BeaconData beaconData) {
        if (!container.contains(Keys.BEACON_PRIMARY_EFFECT.getQuery()) && !container.contains(Keys.BEACON_SECONDARY_EFFECT.getQuery())) {
            return Optional.of(beaconData);
        }
        if (container.contains(Keys.BEACON_PRIMARY_EFFECT.getQuery())) {
            PotionEffectType type =
                    Sponge.getRegistry().getType(PotionEffectType.class, container.getString(Keys.BEACON_PRIMARY_EFFECT.getQuery()).get()).get();
            beaconData.primaryEffect().set(Optional.of(type));
        }
        if (container.contains(Keys.BEACON_SECONDARY_EFFECT.getQuery())) {
            PotionEffectType type =
                    Sponge.getRegistry().getType(PotionEffectType.class, container.getString(Keys.BEACON_SECONDARY_EFFECT.getQuery()).get()).get();
            beaconData.secondaryEffect().set(Optional.of(type));
        }
        return Optional.of(beaconData);
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }
}
