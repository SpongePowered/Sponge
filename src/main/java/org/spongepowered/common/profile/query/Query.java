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
import com.google.common.collect.Sets;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.GameProfileCache;
import org.spongepowered.api.profile.ProfileNotFoundException;
import org.spongepowered.api.util.Identifiable;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class Query<V> implements Callable<V> {

    protected GameProfileCache cache;
    protected final boolean useCache;

    public Query(GameProfileCache cache, boolean useCache) {
        this.cache = cache;
        this.useCache = useCache;
    }

    protected List<GameProfile> fromUniqueIds(Collection<UUID> uniqueIds) throws ProfileNotFoundException {
        final Set<UUID> mutableIds = Sets.newHashSet(uniqueIds);
        final List<GameProfile> result = Lists.newArrayList();

        if (this.useCache) {
            Set<UUID> cached = this.cache.getProfiles().stream()
                    .map(Identifiable::getUniqueId)
                    .filter(uniqueId -> uniqueId != null)
                    .collect(Collectors.toSet());
            mutableIds.stream()
                    .filter(cached::contains)
                    .forEach(uniqueId -> this.cache.getById(uniqueId).ifPresent(profile -> {
                        result.add(profile);
                        mutableIds.remove(uniqueId);
                    }));

            if (mutableIds.isEmpty()) {
                return result;
            }
        }

        result.addAll(this.cache.getByIds(mutableIds).entrySet().stream()
                .map(entry -> entry.getValue().orElse(null))
                .filter(profile -> profile != null)
                .collect(Collectors.toList()));

        return result;
    }

    protected GameProfile fillProfile(GameProfile profile, boolean signed) throws ProfileNotFoundException {
        if (this.useCache) {
            Optional<GameProfile> result = this.cache.getById(profile.getUniqueId());
            if (result.isPresent() && result.get().isFilled()) {
                return result.get();
            }
        }

        Optional<GameProfile> result = this.cache.fillProfile(profile, signed);
        if (result.isPresent() && result.get().isFilled()) {
            GameProfile t = result.get();

            this.cache.add(t);

            return t;
        } else {
            throw new ProfileNotFoundException("Profile: " + profile);
        }
    }

    protected List<GameProfile> fromNames(Collection<String> names) throws ProfileNotFoundException {
        final Set<String> mutableNames = Sets.newHashSet(names);
        final List<GameProfile> result = Lists.newArrayList();

        if (this.useCache) {
            Set<String> cached = this.cache.getProfiles().stream()
                    .filter(profile -> profile.getName().isPresent())
                    .map(profile -> profile.getName().get())
                    .collect(Collectors.toSet());
            mutableNames.stream()
                    .filter(name -> cached.contains(name.toLowerCase(Locale.ROOT)))
                    .forEach(name -> this.cache.getByName(name).ifPresent(profile -> {
                        result.add(profile);
                        mutableNames.remove(name);
                    }));

            if (mutableNames.isEmpty()) {
                return result;
            }
        }

        result.addAll(this.cache.getByNames(mutableNames).entrySet().stream()
                .map(entry -> entry.getValue().orElse(null))
                .filter(profile -> profile != null)
                .collect(Collectors.toList()));

        return result;
    }

}
