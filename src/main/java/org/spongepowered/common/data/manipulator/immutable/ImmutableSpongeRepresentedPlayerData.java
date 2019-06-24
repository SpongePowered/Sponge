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
package org.spongepowered.common.data.manipulator.immutable;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableRepresentedPlayerData;
import org.spongepowered.api.data.manipulator.mutable.RepresentedPlayerData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.common.data.manipulator.mutable.SpongeRepresentedPlayerData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.util.Constants;

public class ImmutableSpongeRepresentedPlayerData
        extends AbstractImmutableSingleData<GameProfile, ImmutableRepresentedPlayerData, RepresentedPlayerData>
        implements ImmutableRepresentedPlayerData {

    private final ImmutableValue<GameProfile> immutableValue = new ImmutableSpongeValue<>(this.usedKey, SpongeRepresentedPlayerData.NULL_PROFILE, this.value);

    public ImmutableSpongeRepresentedPlayerData() {
        this(SpongeRepresentedPlayerData.NULL_PROFILE);
    }

    public ImmutableSpongeRepresentedPlayerData(GameProfile ownerId) {
        super(ImmutableRepresentedPlayerData.class, ownerId, Keys.REPRESENTED_PLAYER);
    }

    @Override
    public ImmutableValue<GameProfile> owner() {
        return this.immutableValue;
    }

    @Override
    public DataContainer toContainer() {
        final DataContainer container = super.toContainer();
        if (this.value.getUniqueId() != null) {
            container.set(this.usedKey.getQuery().then(Constants.GameProfile.GAME_PROFILE_ID), this.value.getUniqueId().toString());
        }
        if (this.value.getName().isPresent()) {
            container.set(this.usedKey.getQuery().then(Constants.GameProfile.GAME_PROFILE_NAME), this.value.getName().get());
        }
        return container;
    }

    @Override
    protected ImmutableValue<?> getValueGetter() {
        return this.owner();
    }

    @Override
    public RepresentedPlayerData asMutable() {
        return new SpongeRepresentedPlayerData(this.value);
    }

}
