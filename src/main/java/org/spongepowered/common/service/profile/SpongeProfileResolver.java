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
package org.spongepowered.common.service.profile;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.mojang.authlib.Agent;
import com.mojang.authlib.ProfileLookupCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import org.spongepowered.api.GameProfile;
import org.spongepowered.api.service.profile.GameProfileResolver;
import org.spongepowered.api.service.profile.ProfileNotFoundException;
import org.spongepowered.common.Sponge;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class SpongeProfileResolver implements GameProfileResolver {

    private static abstract class Query<V> implements Callable<V> {

        private final boolean useCache;
        protected final MinecraftServer server = MinecraftServer.getServer();
        protected final PlayerProfileCache cache = this.server.getPlayerProfileCache();

        public Query(boolean useCache) {
            this.useCache = useCache;
        }

        protected GameProfile fromId(UUID id) throws Exception {
            if (this.useCache) {
                GameProfile profile = (GameProfile) this.cache.getProfileByUUID(id);
                if (profile != null) {
                    return profile;
                }
            }
            if (Sponge.getGame().getPlatform().getType().isClient()) {
                /* TODO: This will only compile on the client (in SpongeForge)
                com.mojang.authlib.GameProfile sessionProfile = Minecraft.getMinecraft().getSession().getProfile();
                if (sessionProfile.getId().equals(id)) {
                    return (GameProfile) sessionProfile;
                }*/
            }
            // TODO Possibly use UUID -> Name History
            // (http://wiki.vg/Mojang_API#UUID_-.3E_Name_history)
            com.mojang.authlib.GameProfile profile =
                    this.server.getMinecraftSessionService().fillProfileProperties(new com.mojang.authlib.GameProfile(id, null), false);
            if (profile == null || !profile.isComplete()) {
                throw new ProfileNotFoundException("Profile: " + profile);
            }
            this.cache.addEntry(profile);
            return (GameProfile) profile;
        }

        protected List<GameProfile> fromNames(List<String> names) throws Exception {
            final List<GameProfile> profiles = Lists.newArrayList();
            Set<String> cachedNames = Sets.newHashSet(this.cache.getUsernames());
            if (this.useCache) {
                for (int i = 0; i < names.size(); i++) {
                    GameProfile profile = null;
                    if (cachedNames.contains(names.get(i).toLowerCase(Locale.ROOT))) {
                        profile = (GameProfile) this.cache.getGameProfileForUsername(names.get(i));
                    }
                    if (profile != null) {
                        profiles.add(profile);
                        names.remove(i--);
                    }
                }
            }
            if (names.isEmpty()) {
                return profiles;
            }
            final ProfileNotFoundException[] thrown = new ProfileNotFoundException[1];
            this.server.getGameProfileRepository().findProfilesByNames(names.toArray(new String[0]), Agent.MINECRAFT, new ProfileLookupCallback() {

                @Override
                public void onProfileLookupSucceeded(com.mojang.authlib.GameProfile profile) {
                    Query.this.cache.addEntry(profile);
                    if (thrown[0] == null) {
                        profiles.add((GameProfile) profile);
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
            return profiles;
        }

    }

    private static class SingleQuery extends Query<GameProfile> {

        private final UUID id;
        private final String name;

        public SingleQuery(UUID uniqueId, boolean useCache) {
            super(useCache);
            this.id = uniqueId;
            this.name = null;
        }

        public SingleQuery(String name, boolean useCache) {
            super(useCache);
            this.name = name;
            this.id = null;
        }

        @Override
        public GameProfile call() throws Exception {
            if (this.id != null) {
                return this.fromId(this.id);
            } else if (this.name != null) {
                return this.fromNames(Lists.newArrayList(this.name)).get(0);
            }
            throw new IllegalStateException("Impossible! The query if not for a name or a uuid.");
        }

    }

    private static class MultiQuery extends Query<Collection<GameProfile>> {

        private final Iterator<?> iterator;

        public MultiQuery(Iterable<?> iterable, boolean useCache) {
            super(useCache);
            this.iterator = iterable.iterator();
        }

        @Override
        public Collection<GameProfile> call() throws Exception {
            if (!this.iterator.hasNext()) {
                return Collections.emptyList();
            }
            Object first = null; // Isn't type erasure great?!
            do {
                first = this.iterator.next();
            } while (first == null && this.iterator.hasNext());
            if (first instanceof String) {
                @SuppressWarnings("unchecked")
                List<String> names = Lists.newArrayList((Iterator<String>) this.iterator);
                names.add((String) first);
                return this.fromNames(names);
            } else if (first instanceof UUID) {
                return this.iterateUuids((UUID) first);
            }
            return Collections.emptyList();
        }

        private Collection<GameProfile> iterateUuids(UUID first) throws Exception {
            Collection<GameProfile> profiles = Lists.newArrayList(this.fromId(first));
            while (this.iterator.hasNext()) {
                profiles.add(this.fromId((UUID) this.iterator.next()));
            }
            return profiles;
        }
    }

    private final ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

    @Override
    public ListenableFuture<GameProfile> get(UUID uniqueId) {
        return this.get(uniqueId, true);
    }

    @Override
    public ListenableFuture<GameProfile> get(UUID uniqueId, final boolean useCache) {
        return this.executor.submit(new SingleQuery(checkNotNull(uniqueId, "uniqueId"), useCache));
    }

    @Override
    public ListenableFuture<GameProfile> get(String name) {
        return this.get(name, true);
    }

    @Override
    public ListenableFuture<GameProfile> get(String name, boolean useCache) {
        return this.executor.submit(new SingleQuery(checkNotNull(name, "name"), useCache));
    }

    @Override
    public ListenableFuture<Collection<GameProfile>> getAllByName(Iterable<String> names, boolean useCache) {
        return this.executor.submit(new MultiQuery(checkNotNull(names, "names"), useCache));
    }

    @Override
    public ListenableFuture<Collection<GameProfile>> getAllById(Iterable<UUID> uniqueIds, boolean useCache) {
        return this.executor.submit(new MultiQuery(checkNotNull(uniqueIds, "uniqueIds"), useCache));
    }

    @Override
    public Collection<GameProfile> getCachedProfiles() {
        PlayerProfileCache cache = MinecraftServer.getServer().getPlayerProfileCache();
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
            if (profile.getName().startsWith(lastKnownName)) {
                matching.add(profile);
            }
        }
        return matching;
    }

    // Internal. Get the profile from the UUID and block until a result
    public static GameProfile getProfile(UUID uniqueId, boolean useCache) {
        try {
            return new SingleQuery(uniqueId, useCache).call();
        } catch (Exception e) {
            Sponge.getLogger().warn("Failed to lookup game profile for {}", uniqueId, e);
            return null;
        }
    }

}
