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
package org.spongepowered.common.data.builder.manipulator.mutable.tileentity;

import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntityBeacon;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableBeaconData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.BeaconData;
import org.spongepowered.api.potion.PotionEffectType;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeBeaconData;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.interfaces.tileentity.IMixinTileEntityBeacon;

import java.util.Optional;

public class BeaconDataBuilder implements DataManipulatorBuilder<BeaconData, ImmutableBeaconData> {

    @Override
    public BeaconData create() {
        return new SpongeBeaconData();
    }

    @Override
    public Optional<BeaconData> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof TileEntityBeacon) {
            Optional<PotionEffectType> primary =
                    Optional.of(
                            (PotionEffectType) Potion.potionTypes[((IMixinTileEntityBeacon) dataHolder).getPrimaryEffect()]);
            Optional<PotionEffectType> secondary =
                    Optional.of(
                            (PotionEffectType) Potion.potionTypes[((IMixinTileEntityBeacon) dataHolder).getSecondaryEffect()]);
            return Optional.of(new SpongeBeaconData(primary, secondary));
        }
        return Optional.empty();
    }

    @Override
    public Optional<BeaconData> build(DataView container) throws InvalidDataException {
        DataUtil.checkDataExists(container, Keys.BEACON_PRIMARY_EFFECT.getQuery());
        DataUtil.checkDataExists(container, Keys.BEACON_SECONDARY_EFFECT.getQuery());

        Optional<PotionEffectType> primary =
                (Optional<PotionEffectType>) container.get(Keys.BEACON_PRIMARY_EFFECT.getQuery()).get();
        Optional<PotionEffectType> secondary =
                (Optional<PotionEffectType>) container.get(Keys.BEACON_SECONDARY_EFFECT.getQuery()).get();
        return Optional.of(new SpongeBeaconData(primary, secondary));
    }
}
