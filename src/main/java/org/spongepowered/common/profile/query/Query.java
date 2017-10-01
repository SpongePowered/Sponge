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

import com.google.common.collect.Lists;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.GameProfileCache;
import org.spongepowered.api.profile.ProfileNotFoundException;
import org.spongepowered.common.util.SpongeUsernameCache;

import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public abstract class Query<V> implements Callable<V> {

    protected GameProfileCache cache;
    protected final boolean useCache;

    public Query(GameProfileCache cache, boolean useCache) {
        this.cache = cache;
        this.useCache = useCache;
    }

    protected List<GameProfile> fromUniqueIds(Collection<UUID> uniqueIds) throws ProfileNotFoundException {
        if (this.useCache) {
            List<UUID> pool = Lists.newArrayList(uniqueIds);
            List<GameProfile> result = Lists.newArrayListWithCapacity(uniqueIds.size());

            // check username cache first
            Iterator<UUID> it = pool.iterator();
            while (it.hasNext()) {
                UUID uniqueId = it.next();
                @Nullable String username = SpongeUsernameCache.getLastKnownUsername(uniqueId);
                if (username != null) {
                    result.add(GameProfile.of(uniqueId, username));
                    it.remove();
                }
            }

            if (!pool.isEmpty()) {
                result.addAll(this.cache.getOrLookupByIds(pool).values().stream().filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList()));
            }

            return result;
        }

        return this.cache.lookupByIds(uniqueIds).values().stream().filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }

    protected GameProfile fillProfile(GameProfile profile, boolean signed) throws ProfileNotFoundException {
        if (this.useCache) {
            Optional<GameProfile> result = this.cache.getById(profile.getUniqueId());
            if (result.isPresent() && result.get().isFilled() && !result.get().getPropertyMap().isEmpty()) {
                return result.get();
            }
        }

        Optional<GameProfile> result = this.cache.fillProfile(profile, signed);
        if (result.isPresent() && result.get().isFilled()) {
            GameProfile filled = result.get();

            this.cache.add(filled, true, (Instant) null);

            return filled;
        }
        throw new ProfileNotFoundException("Profile: " + profile);
    }

    protected List<GameProfile> fromNames(Collection<String> names) throws ProfileNotFoundException {
        if (this.useCache) {
            return this.cache.getOrLookupByNames(names).values().stream().filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
        }
        return this.cache.lookupByNames(names).values().stream().filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }

}
