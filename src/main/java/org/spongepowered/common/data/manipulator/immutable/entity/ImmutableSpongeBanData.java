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
package org.spongepowered.common.data.manipulator.immutable.entity;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableBanData;
import org.spongepowered.api.data.manipulator.mutable.entity.BanData;
import org.spongepowered.api.data.value.immutable.ImmutableSetValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeBanData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeSetValue;

import java.util.Set;

public class ImmutableSpongeBanData extends AbstractImmutableSingleData<Set<Ban.User>, ImmutableBanData, BanData>
        implements ImmutableBanData {

    public ImmutableSpongeBanData(Set<Ban.User> bans) {
        super(ImmutableBanData.class, bans, Keys.USER_BANS);
    }

    @Override
    protected ImmutableValue<?> getValueGetter() {
        return bans();
    }

    @Override
    public BanData asMutable() {
        return new SpongeBanData(this.getValue());
    }

    @Override
    public ImmutableSetValue<Ban.User> bans() {
        return new ImmutableSpongeSetValue<Ban.User>(Keys.USER_BANS, this.getValue());
    }

    @Override
    public int compareTo(ImmutableBanData o) {
        return 0;
    }
}
