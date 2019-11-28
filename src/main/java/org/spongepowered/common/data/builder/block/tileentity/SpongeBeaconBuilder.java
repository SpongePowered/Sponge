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
package org.spongepowered.common.data.builder.block.tileentity;

import org.spongepowered.api.block.tileentity.carrier.Beacon;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.tileentity.BeaconData;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeBeaconData;
import org.spongepowered.common.util.Constants;

import java.util.Optional;
import net.minecraft.potion.Effect;
import net.minecraft.tileentity.BeaconTileEntity;

public class SpongeBeaconBuilder extends SpongeLockableBuilder<Beacon> {

    public SpongeBeaconBuilder() {
        super(Beacon.class, 1);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected Optional<Beacon> buildContent(DataView container) throws InvalidDataException {
        return super.buildContent(container).flatMap(beacon -> {
            final BeaconData beaconData = new SpongeBeaconData();
            container.getInt(Constants.TileEntity.Beacon.PRIMARY)
                .map(Effect::get)
                .map(potion -> (PotionEffectType) potion)
                .ifPresent(potion -> beaconData.set(Keys.BEACON_PRIMARY_EFFECT, Optional.of(potion)));
            container.getInt(Constants.TileEntity.Beacon.SECONDARY)
                .map(Effect::get)
                .map(potion -> (PotionEffectType) potion)
                .ifPresent(potion -> beaconData.set(Keys.BEACON_SECONDARY_EFFECT, Optional.of(potion)));
            beacon.offer(beaconData);
            ((BeaconTileEntity) beacon).validate();
            return Optional.of(beacon);
        });
    }
}
