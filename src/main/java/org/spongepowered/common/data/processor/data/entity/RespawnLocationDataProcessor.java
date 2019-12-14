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
package org.spongepowered.common.data.processor.data.entity;

import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableRespawnLocation;
import org.spongepowered.api.data.manipulator.mutable.entity.RespawnLocationData;
import org.spongepowered.api.data.value.MapValue.Immutable;
import org.spongepowered.api.data.value.MapValue.Mutable;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.util.RespawnLocation;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeRespawnLocationData;
import org.spongepowered.common.data.processor.common.AbstractSingleDataSingleTargetProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeMapValue;
import org.spongepowered.common.data.value.mutable.SpongeMapValue;
import org.spongepowered.common.bridge.entity.player.BedLocationHolder;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class RespawnLocationDataProcessor extends
        AbstractSingleDataSingleTargetProcessor<User, Map<UUID, RespawnLocation>, Mutable<UUID, RespawnLocation>, RespawnLocationData, ImmutableRespawnLocation> {

    public RespawnLocationDataProcessor() {
        super(Keys.RESPAWN_LOCATIONS, User.class);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (container instanceof BedLocationHolder) {
            ImmutableMap<UUID, RespawnLocation> removed = ((BedLocationHolder) container).bridge$removeAllBeds();
            if (!removed.isEmpty()) {
                return DataTransactionResult.successRemove(this.constructImmutableValue(removed));
            }
            return DataTransactionResult.successNoData();
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    protected boolean set(User user, Map<UUID, RespawnLocation> value) {
        if (user instanceof BedLocationHolder) {
            return ((BedLocationHolder) user).bridge$setBedLocations(value);
        }
        return false;
    }

    @Override
    protected Optional<Map<UUID, RespawnLocation>> getVal(User user) {
        if (user instanceof BedLocationHolder) {
            return Optional.of(((BedLocationHolder) user).bridge$getBedlocations());
        }
        return Optional.empty();
    }

    @Override
    protected Mutable<UUID, RespawnLocation> constructValue(Map<UUID, RespawnLocation> actualValue) {
        return new SpongeMapValue<>(Keys.RESPAWN_LOCATIONS, actualValue);
    }

    @Override
    protected Immutable<UUID, RespawnLocation> constructImmutableValue(Map<UUID, RespawnLocation> value) {
        return new ImmutableSpongeMapValue<>(Keys.RESPAWN_LOCATIONS, value);
    }

    @Override
    protected RespawnLocationData createManipulator() {
        return new SpongeRespawnLocationData();
    }

}
