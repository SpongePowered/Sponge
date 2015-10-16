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
package org.spongepowered.common.data.processor.data.tileentity;

import net.minecraft.tileentity.TileEntityBeacon;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableBeaconData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.BeaconData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeBeaconData;
import org.spongepowered.common.data.processor.common.AbstractSpongeDataProcessor;

import java.util.Optional;

public class BeaconDataProcessor extends AbstractSpongeDataProcessor<BeaconData, ImmutableBeaconData> {

    @Override
    public boolean supports(DataHolder dataHolder) {
        return dataHolder instanceof TileEntityBeacon;
    }

    @Override
    public Optional<BeaconData> from(DataHolder dataHolder) {
        if (dataHolder instanceof TileEntityBeacon) {
            TileEntityBeacon beacon = (TileEntityBeacon) dataHolder;
            return Optional.of(new SpongeBeaconData(beacon)); // temp
        }
        return Optional.empty();
    }

    @Override
    public Optional<BeaconData> createFrom(DataHolder dataHolder) {
        return this.from(dataHolder);
    }

    @Override
    public Optional<BeaconData> fill(DataHolder dataHolder, BeaconData manipulator, MergeFunction overlap) {
        return null;
    }

    @Override
    public Optional<BeaconData> fill(DataContainer container, BeaconData beaconData) {
        return null;
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, BeaconData manipulator, MergeFunction function) {
        return null;
    }

    @Override
    public Optional<ImmutableBeaconData> with(Key<? extends BaseValue<?>> key, Object value,
            ImmutableBeaconData immutable) {
        return null;
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return null;
    }
}
