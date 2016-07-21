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
package org.spongepowered.common.profile.query;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.GameProfileCache;
import org.spongepowered.common.util.SpongeUsernameCache;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public abstract class UniqueIdQuery<T> extends Query<T> {

    protected UniqueIdQuery(GameProfileCache cache, boolean useCache) {
        super(cache, useCache);
    }

    public static final class SingleGet extends UniqueIdQuery<GameProfile> {

        private final UUID uniqueId;

        public SingleGet(GameProfileCache cache, UUID uniqueId, boolean useCache) {
            super(cache, useCache);
            this.uniqueId = uniqueId;
        }

        @Override
        public GameProfile call() throws Exception {
            if (this.useCache) {
                // check username cache first
                String username = SpongeUsernameCache.getLastKnownUsername(this.uniqueId);
                if (username != null) {
                    return GameProfile.of(this.uniqueId, username);
                }
            }

            final List<GameProfile> gameProfiles = this.fromUniqueIds(Collections.singleton(this.uniqueId));
            return gameProfiles.isEmpty() ? GameProfile.of(this.uniqueId, null) : gameProfiles.get(0);
        }
    }

    public static final class MultiGet extends UniqueIdQuery<Collection<GameProfile>> {

        private final Iterator<UUID> iterator;

        public MultiGet(GameProfileCache cache, Iterable<UUID> iterable, boolean useCache) {
            super(cache, useCache);
            this.iterator = iterable.iterator();
        }

        @Override
        public Collection<GameProfile> call() throws Exception {
            if (!this.iterator.hasNext()) {
                return ImmutableSet.of();
            }

            return this.fromUniqueIds(Sets.newHashSet(this.iterator));
        }
    }

}
