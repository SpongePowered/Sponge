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

import net.minecraft.tileentity.TileEntityBeacon;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableBeaconData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.BeaconData;
import org.spongepowered.api.data.value.immutable.ImmutableOptionalValue;
import org.spongepowered.api.potion.PotionEffectType;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeBeaconData;

public class ImmutableSpongeBeaconData extends AbstractImmutableData<ImmutableBeaconData, BeaconData> implements ImmutableBeaconData {

    private final TileEntityBeacon beacon;

    public ImmutableSpongeBeaconData(TileEntityBeacon beacon) {
        super(ImmutableBeaconData.class);
        this.beacon = beacon;
        this.registerGetters();
    }

    @Override
    protected void registerGetters() {

    }

    @Override
    public ImmutableOptionalValue<PotionEffectType> primaryEffect() {
        return null;
    }

    @Override
    public ImmutableOptionalValue<PotionEffectType> secondaryEffect() {
        return null;
    }

    @Override
    public ImmutableBeaconData clearEffects() {
        return null;
    }

    @Override
    public BeaconData asMutable() {
        return new SpongeBeaconData(this.beacon);
    }

    @Override
    public int compareTo(ImmutableBeaconData o) {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        return null;
    }
}
