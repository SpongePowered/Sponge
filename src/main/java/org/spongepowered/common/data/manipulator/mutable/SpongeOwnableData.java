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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.GameProfile;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableOwnableData;
import org.spongepowered.api.data.manipulator.mutable.OwnableData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeOwnableData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongeValue;

public class SpongeOwnableData extends AbstractData<OwnableData, ImmutableOwnableData> implements OwnableData {

    private GameProfile profile;

    public SpongeOwnableData(GameProfile profile) {
        super(OwnableData.class);
        this.profile = checkNotNull(profile);
        registerGettersAndSetters();
    }

    @Override
    public Value<GameProfile> profile() {
        return new SpongeValue<GameProfile>(Keys.OWNED_BY_PROFILE, this.profile);
    }

    @Override
    public OwnableData copy() {
        return new SpongeOwnableData(this.profile);
    }

    @Override
    public ImmutableOwnableData asImmutable() {
        return new ImmutableSpongeOwnableData(this.profile);
    }

    @Override
    public int compareTo(OwnableData o) {
        return ComparisonChain.start()
                .compare(o.profile().get().getUniqueId(), this.profile.getUniqueId())
                .compare(o.profile().get().getName(), this.profile.getName())
                .result();
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
            .set(Keys.OWNED_BY_PROFILE.getQuery(), this.profile);
    }

    @Override
    protected void registerGettersAndSetters() {

    }
}
