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
import com.mojang.authlib.Agent;
import com.mojang.authlib.ProfileLookupCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.ProfileNotFoundException;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

public abstract class Query<V> implements Callable<V> {

    protected final boolean useCache;
    protected final MinecraftServer server = (MinecraftServer) Sponge.getServer();
    protected final PlayerProfileCache cache = this.server.getPlayerProfileCache();

    public Query(boolean useCache) {
        this.useCache = useCache;
    }

    protected GameProfile fromUniqueId(UUID uniqueId) throws ProfileNotFoundException {
        return this.fillProfile((GameProfile) new com.mojang.authlib.GameProfile(uniqueId, null), false);
    }

    protected GameProfile fillProfile(GameProfile profile, boolean signed) throws ProfileNotFoundException {
        if (this.useCache) {
            GameProfile result = (GameProfile) this.cache.getProfileByUUID(profile.getUniqueId());
            if (result != null) {
                return result;
            }
        }

        com.mojang.authlib.GameProfile result = this.server.getMinecraftSessionService().fillProfileProperties((com.mojang.authlib.GameProfile) profile, signed);
        if (result == null || !result.isComplete()) {
            throw new ProfileNotFoundException("Profile: " + result);
        }

        this.cache.addEntry(result);

        return (GameProfile) result;
    }

    protected List<GameProfile> fromNames(Set<String> names) throws ProfileNotFoundException {
        final Set<String> mutableNames = Sets.newHashSet(names);
        final List<GameProfile> result = Lists.newArrayList();
        if (this.useCache) {
            Set<String> cached = Sets.newHashSet(this.cache.getUsernames());
            for (String name : mutableNames) {
                GameProfile profile = null;
                if (cached.contains(name.toLowerCase(Locale.ROOT))) {
                    profile = (GameProfile) this.cache.getGameProfileForUsername(name);
                }

                if (profile != null) {
                    result.add(profile);
                    mutableNames.remove(name);
                }
            }
        }

        if (mutableNames.isEmpty()) {
            return result;
        }

        final ProfileNotFoundException[] thrown = new ProfileNotFoundException[1];
        this.server.getGameProfileRepository().findProfilesByNames(mutableNames.toArray(new String[mutableNames.size()]), Agent.MINECRAFT, new ProfileLookupCallback() {

            @Override
            public void onProfileLookupSucceeded(com.mojang.authlib.GameProfile profile) {
                Query.this.cache.addEntry(profile);
                if (thrown[0] == null) {
                    result.add((GameProfile) profile);
                }
            }

            @Override
            public void onProfileLookupFailed(com.mojang.authlib.GameProfile profile, Exception exception) {
                thrown[0] = new ProfileNotFoundException("Profile: " + profile, exception);
            }
        });

        this.cache.save();

        if (thrown[0] != null) {
            throw thrown[0];
        }

        return result;
    }

}
