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
package org.spongepowered.common.data.manipulator.mutable;

import com.google.common.collect.Multimap;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableRepresentedPlayerData;
import org.spongepowered.api.data.manipulator.mutable.RepresentedPlayerData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeRepresentedPlayerData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.util.Constants;

import java.util.Optional;
import java.util.UUID;

public class SpongeRepresentedPlayerData extends AbstractSingleData<GameProfile, RepresentedPlayerData, ImmutableRepresentedPlayerData>
        implements RepresentedPlayerData {

    /**
     * Really problematic way to create a dummy profile.
     *
     * Processors must make sure this value never reaches an actual DataHolder
     */
    public static final GameProfile NULL_PROFILE = new GameProfile() {
        @Override
        public UUID getUniqueId() {
            return null;
        }

        @Override
        public int getContentVersion() {
            return 0;
        }

        @Override
        public DataContainer toContainer() {
            return DataContainer.createNew().set(Queries.CONTENT_VERSION, this.getContentVersion());
        }

        @Override
        public Optional<String> getName() {
            return Optional.empty();
        }

        @Override
        public Multimap<String, ProfileProperty> getPropertyMap() {
            return null;
        }

        @Override
        public boolean isFilled() {
            return false;
        }
    };

    public SpongeRepresentedPlayerData() {
        this(NULL_PROFILE);
    }

    public SpongeRepresentedPlayerData(GameProfile profile) {
        super(RepresentedPlayerData.class, profile, Keys.REPRESENTED_PLAYER);
    }

    @Override
    public Value<GameProfile> owner() {
        return new SpongeValue<>(this.usedKey, this.getValue());
    }

    @Override
    public RepresentedPlayerData copy() {
        return new SpongeRepresentedPlayerData(this.getValue());
    }

    @Override
    public DataContainer toContainer() {
        final DataContainer container = super.toContainer();
        if (this.getValue().getUniqueId() != null) {
            container.set(this.usedKey.getQuery().then(Constants.GameProfile.GAME_PROFILE_ID), this.getValue().getUniqueId().toString());
        }
        if (this.getValue().getName().isPresent()) {
            container.set(this.usedKey.getQuery().then(Constants.GameProfile.GAME_PROFILE_NAME), this.getValue().getName().get());
        }
        return container;
    }

    @Override
    protected Value<?> getValueGetter() {
        return this.owner();
    }

    @Override
    public ImmutableRepresentedPlayerData asImmutable() {
        return new ImmutableSpongeRepresentedPlayerData(this.getValue());
    }

}
