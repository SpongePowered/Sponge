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
package org.spongepowered.common.mixin.api.mcp.server.management;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import net.minecraft.server.management.PlayerProfileCache;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.GameProfileCache;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.server.management.PlayerProfileCache_ProfileEntryAccessor;
import org.spongepowered.common.profile.SpongeGameProfile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

@Mixin(PlayerProfileCache.class)
public abstract class PlayerProfileCacheMixin_API implements GameProfileCache {

    @Shadow @Final @Mutable private final Map<String, PlayerProfileCache_ProfileEntryAccessor> usernameToProfileEntryMap = new ConcurrentHashMap<>();
    @Shadow @Final @Mutable private final Map<UUID, PlayerProfileCache_ProfileEntryAccessor> uuidToProfileEntryMap = new ConcurrentHashMap<>();
    @Shadow @Final @Mutable private final Deque<com.mojang.authlib.GameProfile> gameProfiles = new ConcurrentLinkedDeque<>();

    @Nullable @Shadow public abstract com.mojang.authlib.GameProfile shadow$getProfileByUUID(UUID uniqueId);

    @Override
    public boolean remove(final GameProfile profile) {
        Objects.requireNonNull(profile, "profile");
        final UUID uniqueId = profile.getUniqueId();
        final PlayerProfileCache_ProfileEntryAccessor entry = this.uuidToProfileEntryMap.remove(uniqueId);
        if (entry != null) {
            if (profile.getName().isPresent()) {
                this.usernameToProfileEntryMap.remove(profile.getName().get().toLowerCase(Locale.ROOT));
            }
            this.gameProfiles.remove(entry.accessor$getGameProfile());
            // Only return true if the entry wasn't expired
            return entry.accessor$getExpirationDate().getTime() >= System.currentTimeMillis();
        }
        return false;
    }

    @Override
    public Collection<GameProfile> remove(final Iterable<GameProfile> profiles) {
        Objects.requireNonNull(profiles, "profiles");
        final Collection<GameProfile> result = new ArrayList<>();
        for (final GameProfile profile : profiles) {
            if (this.remove(profile)) {
                result.add(profile);
            }
        }
        return result;
    }

    @Override
    public Collection<GameProfile> removeIf(final Predicate<GameProfile> filter) {
        Objects.requireNonNull(filter, "filter");
        final Collection<GameProfile> result = new ArrayList<>();
        final Iterator<PlayerProfileCache_ProfileEntryAccessor> it = this.uuidToProfileEntryMap.values().iterator();
        while (it.hasNext()) {
            final PlayerProfileCache_ProfileEntryAccessor entry = it.next();
            final GameProfile profile = SpongeGameProfile.of(entry.accessor$getGameProfile());
            final boolean isExpired = entry.accessor$getExpirationDate().getTime() < System.currentTimeMillis();
            if (isExpired || filter.test(profile)) {
                it.remove();
                profile.getName().ifPresent(name -> this.usernameToProfileEntryMap.remove(name, entry));
                this.gameProfiles.remove(entry.accessor$getGameProfile());
                if (!isExpired) {
                    result.add(profile);
                }
            }
        }
        return result;
    }

    @Override
    public void clear() {
        this.uuidToProfileEntryMap.clear();
        this.usernameToProfileEntryMap.clear();
    }

    @Override
    public Optional<GameProfile> getById(final UUID uniqueId) {
        Objects.requireNonNull(uniqueId, "uniqueId");
        return Optional.ofNullable(this.shadow$getProfileByUUID(uniqueId)).map(SpongeGameProfile::of);
    }

    @Override
    public Map<UUID, Optional<GameProfile>> getByIds(final Iterable<UUID> uniqueIds) {
        Objects.requireNonNull(uniqueIds, "uniqueIds");
        final Map<UUID, Optional<GameProfile>> result = new HashMap<>();
        for (final UUID uniqueId : uniqueIds) {
            result.put(uniqueId, Optional.ofNullable(this.shadow$getProfileByUUID(uniqueId)).map(SpongeGameProfile::of));
        }
        return result.isEmpty() ? ImmutableMap.of() : ImmutableMap.copyOf(result);
    }

    @Override
    public Optional<GameProfile> getByName(final String name) {
        Objects.requireNonNull(name, "name");
        @Nullable PlayerProfileCache_ProfileEntryAccessor entry = this.usernameToProfileEntryMap.get(name.toLowerCase(Locale.ROOT));

        if (entry != null && System.currentTimeMillis() >= entry.accessor$getExpirationDate().getTime()) {
            final com.mojang.authlib.GameProfile profile = entry.accessor$getGameProfile();
            this.uuidToProfileEntryMap.remove(profile.getId());
            this.usernameToProfileEntryMap.remove(profile.getName().toLowerCase(Locale.ROOT));
            this.gameProfiles.remove(profile);
            entry = null;
        }

        return entry == null ? Optional.empty() : Optional.of(SpongeGameProfile.of(entry.accessor$getGameProfile()));
    }

    @Override
    public Map<String, Optional<GameProfile>> getByNames(final Iterable<String> names) {
        Objects.requireNonNull(names, "names");
        final Map<String, Optional<GameProfile>> result = Maps.newHashMap();
        for (final String name : names) {
            result.put(name, this.getByName(name));
        }
        return ImmutableMap.copyOf(result);
    }

    @Override
    public Stream<GameProfile> stream() {
        return this.usernameToProfileEntryMap.values().stream()
                .map(entry -> SpongeGameProfile.of(entry.accessor$getGameProfile()));
    }

    @Override
    public Collection<GameProfile> all() {
        return this.stream().collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public Stream<GameProfile> streamOfMatches(final String name) {
        final String search = Objects.requireNonNull(name, "name").toLowerCase(Locale.ROOT);
        return this.stream()
                .filter(profile -> profile.getName().isPresent())
                .filter(profile -> profile.getName().get().toLowerCase(Locale.ROOT).startsWith(search));
    }

    @Override
    public Collection<GameProfile> allMatches(final String name) {
        return this.streamOfMatches(name).collect(ImmutableSet.toImmutableSet());
    }
}
