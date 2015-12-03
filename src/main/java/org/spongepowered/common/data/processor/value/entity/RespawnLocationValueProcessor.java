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
package org.spongepowered.common.data.processor.value.entity;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.MapValue;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeMapValue;
import org.spongepowered.common.data.value.mutable.SpongeMapValue;
import org.spongepowered.common.entity.player.ISpongeUser;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class RespawnLocationValueProcessor extends AbstractSpongeValueProcessor<User, Map<UUID, Vector3d>, MapValue<UUID, Vector3d>> {

    public RespawnLocationValueProcessor() {
        super(User.class, Keys.RESPAWN_LOCATIONS);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (container instanceof ISpongeUser) {
            ImmutableMap<UUID, Vector3d> removed = ((ISpongeUser) container).removeAllBeds();
            if (!removed.isEmpty()) {
                return DataTransactionResult
                    .builder().result(DataTransactionResult.Type.SUCCESS).replace(constructImmutableValue(removed)).build();
            } else {
                return DataTransactionResult.successNoData();
            }
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    protected MapValue<UUID, Vector3d> constructValue(Map<UUID, Vector3d> actualValue) {
        return new SpongeMapValue<>(Keys.RESPAWN_LOCATIONS, actualValue);
    }

    @Override
    protected boolean set(User user, Map<UUID, Vector3d> value) {
        if (user instanceof ISpongeUser) {
            return ((ISpongeUser) user).setBedLocations(value);
        }
        return false;
    }

    @Override
    protected Optional<Map<UUID, Vector3d>> getVal(User user) {
        if (user instanceof ISpongeUser) {
            return Optional.of(((ISpongeUser) user).getBedlocations());
        }
        return Optional.empty();
    }

    @Override
    protected ImmutableValue<Map<UUID, Vector3d>> constructImmutableValue(Map<UUID, Vector3d> value) {
        return new ImmutableSpongeMapValue<>(Keys.RESPAWN_LOCATIONS, value);
    }

}
