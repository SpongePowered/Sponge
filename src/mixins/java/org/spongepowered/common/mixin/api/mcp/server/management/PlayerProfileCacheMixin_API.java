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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.Agent;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.server.management.PlayerProfileCache;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.GameProfileCache;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.server.management.PlayerProfileCache_ProfileEntryAccessor;
import org.spongepowered.common.profile.callback.MapProfileLookupCallback;
import org.spongepowered.common.profile.callback.SingleProfileLookupCallback;
import org.spongepowered.common.util.Constants;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

@Mixin(PlayerProfileCache.class)
public abstract class PlayerProfileCacheMixin_API implements GameProfileCache {

    @Shadow @Final private Map<String, PlayerProfileCache_ProfileEntryAccessor> usernameToProfileEntryMap;
    @Shadow @Final private Map<UUID, PlayerProfileCache_ProfileEntryAccessor> uuidToProfileEntryMap;
    @Nullable @Shadow public abstract com.mojang.authlib.GameProfile shadow$getProfileByUUID(UUID uniqueId);
    @Shadow public abstract void shadow$save();
    @Shadow private void shadow$addEntry(final com.mojang.authlib.GameProfile profile, @Nullable final Date expiry) { }
    // Thread-safe queue
    private Queue<com.mojang.authlib.GameProfile> profiles = new ConcurrentLinkedQueue<>();

    @Override
    public boolean add(final GameProfile profile, final boolean overwrite, @Nullable final Instant expiry) {
        checkNotNull(profile, "profile");

        // Don't attempt to overwrite entries if we aren't requested to do so
        if (this.uuidToProfileEntryMap.containsKey(profile.getUniqueId()) && !overwrite) {
            return false;
        }

        this.shadow$addEntry((com.mojang.authlib.GameProfile) profile, expiry == null ? null : new Date(expiry.toEpochMilli()));

        return true;
    }

    @Override
    public boolean remove(final GameProfile profile) {
        checkNotNull(profile, "profile");

        final UUID uniqueId = profile.getUniqueId();

        if (this.uuidToProfileEntryMap.containsKey(uniqueId)) {
            this.uuidToProfileEntryMap.remove(uniqueId);
            this.profiles.remove(profile);

            if (profile.getName().isPresent()) {
                this.usernameToProfileEntryMap.remove(profile.getName().get().toLowerCase(Locale.ROOT));
            }

            return true;
        }

        return false;
    }

    @Override
    public Collection<GameProfile> remove(final Iterable<GameProfile> profiles) {
        checkNotNull(profiles, "profiles");

        final Collection<GameProfile> result = Lists.newArrayList();

        for (final GameProfile profile : profiles) {
            if (this.remove(profile)) {
                result.add(profile);
            }
        }

        return result;
    }

    @Override
    public void clear() {
        this.uuidToProfileEntryMap.clear();
        this.profiles.clear();
        this.usernameToProfileEntryMap.clear();
        this.shadow$save();
    }

    @Override
    public Optional<GameProfile> getById(final UUID uniqueId) {
        return Optional.ofNullable((GameProfile) this.shadow$getProfileByUUID(checkNotNull(uniqueId, "unique id")));
    }

    @Override
    public Map<UUID, Optional<GameProfile>> getByIds(final Iterable<UUID> uniqueIds) {
        checkNotNull(uniqueIds, "unique ids");

        final Map<UUID, Optional<GameProfile>> result = Maps.newHashMap();

        for (final UUID uniqueId : uniqueIds) {
            result.put(uniqueId, Optional.ofNullable((GameProfile) this.shadow$getProfileByUUID(uniqueId)));
        }

        return result.isEmpty() ? ImmutableMap.of() : ImmutableMap.copyOf(result);
    }

    @Override
    public Optional<GameProfile> lookupById(final UUID uniqueId) {
        checkNotNull(uniqueId, "unique id");

        final com.mojang.authlib.GameProfile profile = SpongeCommon.getServer().getMinecraftSessionService().fillProfileProperties(
                new com.mojang.authlib.GameProfile(uniqueId, ""), true);
        if (profile != null && profile.getName() != null && !profile.getName().isEmpty()) {
            this.shadow$addEntry(profile, null);
            return Optional.of((GameProfile) profile);
        }
        return Optional.empty();
    }

    @Override
    public Map<UUID, Optional<GameProfile>> lookupByIds(final Iterable<UUID> uniqueIds) {
        checkNotNull(uniqueIds, "unique ids");

        final Map<UUID, Optional<GameProfile>> result = Maps.newHashMap();

        final MinecraftSessionService service = SpongeCommon.getServer().getMinecraftSessionService();
        for (final UUID uniqueId : uniqueIds) {
            final com.mojang.authlib.GameProfile profile = service.fillProfileProperties(new com.mojang.authlib.GameProfile(uniqueId, ""), true);
            if (profile != null && profile.getName() != null && !profile.getName().isEmpty()) {
                this.shadow$addEntry(profile, null);
                result.put(uniqueId, Optional.of((GameProfile) profile));
            } else {
                // create a dummy profile to avoid future lookups
                // if actual user logs in, the profile will be updated during PlayerList#initializeConnectionToPlayer
                this.shadow$addEntry(new com.mojang.authlib.GameProfile(uniqueId, Constants.GameProfile.DUMMY_NAME), null);
                result.put(uniqueId, Optional.empty());
            }
        }

        return result.isEmpty() ? ImmutableMap.of() : ImmutableMap.copyOf(result);
    }

    @Override
    public Optional<GameProfile> getOrLookupById(final UUID uniqueId) {
        final Optional<GameProfile> profile = this.getById(uniqueId);
        if (profile.isPresent()) {
            return profile;
        }
        return this.lookupById(uniqueId);
    }

    @Override
    public Map<UUID, Optional<GameProfile>> getOrLookupByIds(final Iterable<UUID> uniqueIds) {
        checkNotNull(uniqueIds, "unique ids");

        final Collection<UUID> pending = Sets.newHashSet(uniqueIds);
        final Map<UUID, Optional<GameProfile>> result = Maps.newHashMap();

        result.putAll(this.getByIds(pending));
        result.forEach((uniqueId, profile) -> {
            if (profile.isPresent()) {
                pending.remove(uniqueId);
            }
        });
        result.putAll(this.lookupByIds(pending));

        return ImmutableMap.copyOf(result);
    }

    @Override
    public Optional<GameProfile> getByName(final String name) {
        return Optional.ofNullable((GameProfile) this.getByNameNoLookup(checkNotNull(name, "name")));
    }

    @Override
    public Map<String, Optional<GameProfile>> getByNames(final Iterable<String> names) {
        checkNotNull(names, "names");

        final Map<String, Optional<GameProfile>> result = Maps.newHashMap();

        for (final String name : names) {
            result.put(name, Optional.ofNullable((GameProfile) this.getByNameNoLookup(name)));
        }

        return result.isEmpty() ? ImmutableMap.of() : ImmutableMap.copyOf(result);
    }

    @Override
    public Optional<GameProfile> lookupByName(final String name) {
        final SingleProfileLookupCallback callback = new SingleProfileLookupCallback();

        SpongeCommon.getServer().getGameProfileRepository().findProfilesByNames(new String[]{name}, Agent.MINECRAFT, callback);

        final Optional<GameProfile> profile = callback.getResult();
        if (profile.isPresent()) {
            this.shadow$addEntry((com.mojang.authlib.GameProfile) profile.get(), null);
        }

        return profile;
    }

    @Override
    public Map<String, Optional<GameProfile>> lookupByNames(final Iterable<String> names) {
        checkNotNull(names, "names");

        final Map<String, Optional<GameProfile>> result = Maps.newHashMap();

        SpongeCommon.getServer().getGameProfileRepository().findProfilesByNames(Iterables.toArray(names, String.class), Agent.MINECRAFT,
                new MapProfileLookupCallback(result));

        if (!result.isEmpty()) {
            for (final Optional<GameProfile> entry : result.values()) {
                if (entry.isPresent()) {
                    this.shadow$addEntry((com.mojang.authlib.GameProfile) entry.get(), null);
                }
            }
            return ImmutableMap.copyOf(result);
        }
        return ImmutableMap.of();
    }

    @Override
    public Optional<GameProfile> getOrLookupByName(final String name) {
        final Optional<GameProfile> profile = this.getByName(name);
        if (profile.isPresent()) {
            return profile;
        }
        return this.lookupByName(name);
    }

    @Override
    public Map<String, Optional<GameProfile>> getOrLookupByNames(final Iterable<String> names) {
        checkNotNull(names, "names");

        final Collection<String> pending = Sets.newHashSet(names);
        final Map<String, Optional<GameProfile>> result = Maps.newHashMap();

        result.putAll(this.getByNames(pending));
        result.forEach((name, profile) -> {
            if (profile.isPresent()) {
                pending.remove(name);
            }
        });
        // lookupByNames can return a map with different keys than the names passes id
        // (in the case where a name it actually capitalized differently). Therefore,
        // lookupByName is used instead here.
        pending.forEach(name -> result.put(name, this.lookupByName(name)));

        return ImmutableMap.copyOf(result);
    }

    @Override
    public Optional<GameProfile> fillProfile(final GameProfile profile, final boolean signed) {
        checkNotNull(profile, "profile");

        return Optional.ofNullable((GameProfile) SpongeCommon.getServer().getMinecraftSessionService()
                .fillProfileProperties((com.mojang.authlib.GameProfile) profile, signed));
    }

    @Override
    public Stream<GameProfile> streamProfiles() {
        return this.usernameToProfileEntryMap.values().stream()
                .map(entry -> (GameProfile) entry.accessor$getGameProfile());
    }

    @Override
    public Collection<GameProfile> getProfiles() {
        return this.streamProfiles().collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public Stream<GameProfile> streamOfMatches(final String name) {
        final String search = checkNotNull(name, "name").toLowerCase(Locale.ROOT);
        return this.streamProfiles()
                .filter(profile -> profile.getName().isPresent())
                .filter(profile -> profile.getName().get().toLowerCase(Locale.ROOT).startsWith(search));
    }

    @Override
    public Collection<GameProfile> match(final String name) {
        return this.streamOfMatches(name).collect(ImmutableSet.toImmutableSet());
    }

    @Nullable
    private com.mojang.authlib.GameProfile getByNameNoLookup(final String username) {
        @Nullable PlayerProfileCache_ProfileEntryAccessor entry = this.usernameToProfileEntryMap.get(username.toLowerCase(Locale.ROOT));

        if (entry != null && System.currentTimeMillis() >= entry.accessor$getExpirationDate().getTime()) {
            final com.mojang.authlib.GameProfile profile = entry.accessor$getGameProfile();
            this.uuidToProfileEntryMap.remove(profile.getId());
            this.usernameToProfileEntryMap.remove(profile.getName().toLowerCase(Locale.ROOT));
            this.profiles.remove(profile);
            entry = null;
        }

        if (entry != null) {
            final com.mojang.authlib.GameProfile profile = entry.accessor$getGameProfile();
            this.profiles.remove(profile);
            this.profiles.add(profile);
        }

        return entry == null ? null : entry.accessor$getGameProfile();
    }

}
