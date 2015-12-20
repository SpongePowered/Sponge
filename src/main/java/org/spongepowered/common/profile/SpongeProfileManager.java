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
package org.spongepowered.common.profile;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.profile.query.GameProfileQuery;
import org.spongepowered.common.profile.query.NameQuery;
import org.spongepowered.common.profile.query.UniqueIdQuery;
import org.spongepowered.common.scheduler.SpongeScheduler;

import java.util.Collection;
import java.util.Locale;
import java.util.UUID;

import javax.annotation.Nullable;

public class SpongeProfileManager implements GameProfileManager {

    private final ListeningExecutorService executor = SpongeScheduler.getInstance().getListeningExecService();

    @Override
    public GameProfile createProfile(UUID uniqueId, @Nullable String name) {
        checkNotNull(uniqueId, "unique id");
        return (GameProfile) new com.mojang.authlib.GameProfile(uniqueId, name);
    }

    @Override
    public ProfileProperty createProfileProperty(String name, String value, @Nullable String signature) {
        return (ProfileProperty) new com.mojang.authlib.properties.Property(checkNotNull(name, "name"), checkNotNull(value, "value"), signature);
    }

    @Override
    public ListenableFuture<GameProfile> get(UUID uniqueId, final boolean useCache) {
        return this.executor.submit(new UniqueIdQuery.SingleGet(checkNotNull(uniqueId, "uniqueId"), useCache));
    }

    @Override
    public ListenableFuture<Collection<GameProfile>> getAllById(Iterable<UUID> uniqueIds, boolean useCache) {
        return this.executor.submit(new UniqueIdQuery.MultiGet(checkNotNull(uniqueIds, "uniqueIds"), useCache));
    }

    @Override
    public ListenableFuture<GameProfile> get(String name, boolean useCache) {
        return this.executor.submit(new NameQuery.SingleGet(checkNotNull(name, "name"), useCache));
    }

    @Override
    public ListenableFuture<Collection<GameProfile>> getAllByName(Iterable<String> names, boolean useCache) {
        return this.executor.submit(new NameQuery.MultiGet(checkNotNull(names, "names"), useCache));
    }

    @Override
    public ListenableFuture<GameProfile> fill(GameProfile profile, boolean signed, boolean useCache) {
        return this.executor.submit(new GameProfileQuery.SingleFill(checkNotNull(profile, "profile"), signed, useCache));
    }

    @Override
    public Collection<GameProfile> getCachedProfiles() {
        PlayerProfileCache cache = ((MinecraftServer) Sponge.getServer()).getPlayerProfileCache();
        Collection<GameProfile> profiles = Lists.newArrayList();
        for (String name : cache.getUsernames()) {
            if (name != null) {
                GameProfile profile = (GameProfile) cache.getGameProfileForUsername(name);
                if (profile != null) {
                    profiles.add(profile);
                }
            }
        }
        return profiles;
    }

    @Override
    public Collection<GameProfile> match(String lastKnownName) {
        lastKnownName = checkNotNull(lastKnownName, "lastKnownName").toLowerCase(Locale.ROOT);
        Collection<GameProfile> allProfiles = this.getCachedProfiles();
        Collection<GameProfile> matching = Sets.newHashSet();
        for (GameProfile profile : allProfiles) {
            if (profile.getName().isPresent() && profile.getName().get().startsWith(lastKnownName)) {
                matching.add(profile);
            }
        }


        return matching;
    }

    // Internal. Get the profile from the UUID and block until a result
    public static GameProfile getProfile(UUID uniqueId, boolean useCache) {
        try {
            return new UniqueIdQuery.SingleGet(uniqueId, useCache).call();
        } catch (Exception e) {
            SpongeImpl.getLogger().warn("Failed to lookup game profile for {}", uniqueId, e);
            return null;
        }
    }

}
