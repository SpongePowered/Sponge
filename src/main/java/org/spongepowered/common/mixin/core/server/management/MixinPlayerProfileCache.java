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
package org.spongepowered.common.mixin.core.server.management;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.server.management.PlayerProfileCache;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.GameProfileCache;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.server.management.IMixinPlayerProfileCache;
import org.spongepowered.common.interfaces.server.management.IMixinPlayerProfileCacheEntry;
import org.spongepowered.common.profile.callback.MapProfileLookupCallback;
import org.spongepowered.common.profile.callback.SingleProfileLookupCallback;
import org.spongepowered.common.util.SpongeUsernameCache;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.Nullable;

@Mixin(PlayerProfileCache.class)
public abstract class MixinPlayerProfileCache implements IMixinPlayerProfileCache, GameProfileCache {

    @Shadow @Final private Map<String, IMixinPlayerProfileCacheEntry> usernameToProfileEntryMap;
    @Shadow @Final private Map<UUID, IMixinPlayerProfileCacheEntry> uuidToProfileEntryMap;
    @Shadow @Final private Deque<com.mojang.authlib.GameProfile> gameProfiles;
    @Nullable @Shadow public abstract com.mojang.authlib.GameProfile getProfileByUUID(UUID uniqueId);
    @Shadow public abstract void save();
    @Shadow private void addEntry(com.mojang.authlib.GameProfile profile, @Nullable Date expiry) { }
    // Thread-safe queue
    private Queue<com.mojang.authlib.GameProfile> profiles = new ConcurrentLinkedQueue<>();
    private boolean canSave = false;

    @Inject(method = "addEntry(Lcom/mojang/authlib/GameProfile;Ljava/util/Date;)V", at = @At(value = "RETURN"))
    public void onAddEntry(com.mojang.authlib.GameProfile profile, Date date, CallbackInfo ci) {
        SpongeUsernameCache.setUsername(profile.getId(), profile.getName());
    }

    @Redirect(method = "addEntry(Lcom/mojang/authlib/GameProfile;Ljava/util/Date;)V", at = @At(value = "INVOKE", target = "Ljava/util/Deque;remove(Ljava/lang/Object;)Z", remap = false))
    public boolean onAddEntryRemove(Deque<com.mojang.authlib.GameProfile> list, Object obj) {
        return this.profiles.remove(obj);
    }

    @Redirect(method = "addEntry(Lcom/mojang/authlib/GameProfile;Ljava/util/Date;)V", at = @At(value = "INVOKE", target = "Ljava/util/Deque;addFirst(Ljava/lang/Object;)V", remap = false))
    public void onAddEntryAdd(Deque<com.mojang.authlib.GameProfile> list, Object obj) {
        this.profiles.add((com.mojang.authlib.GameProfile) obj);
    }

    @Redirect(method = "getGameProfileForUsername", at = @At(value = "INVOKE", target = "Ljava/util/Deque;remove(Ljava/lang/Object;)Z", ordinal = 0, remap = false))
    public boolean onGetGameProfileForUsernameRemove1(Deque<com.mojang.authlib.GameProfile> list, Object obj) {
        return this.profiles.remove(obj);
    }

    @Redirect(method = "getGameProfileForUsername", at = @At(value = "INVOKE", target = "Ljava/util/Deque;remove(Ljava/lang/Object;)Z", ordinal = 1, remap = false))
    public boolean onGetGameProfileForUsernameRemove2(Deque<com.mojang.authlib.GameProfile> list, Object obj) {
        return this.profiles.remove(obj);
    }

    @Redirect(method = "getGameProfileForUsername", at = @At(value = "INVOKE", target = "Ljava/util/Deque;addFirst(Ljava/lang/Object;)V", remap = false))
    public void onGetGameProfileForUsernameAdd(Deque<com.mojang.authlib.GameProfile> list, Object obj) {
        this.profiles.add((com.mojang.authlib.GameProfile) obj);
    }

    @Redirect(method = "getByUUID", at = @At(value = "INVOKE", target = "Ljava/util/Deque;remove(Ljava/lang/Object;)Z", remap = false))
    public boolean onGetByUUIDRemove(Deque<com.mojang.authlib.GameProfile> list, Object obj) {
        return this.profiles.remove(obj);
    }

    @Redirect(method = "getByUUID", at = @At(value = "INVOKE", target = "Ljava/util/Deque;addFirst(Ljava/lang/Object;)V", remap = false))
    public void onGetByUUIDAdd(Deque<com.mojang.authlib.GameProfile> list, Object obj) {
        this.profiles.add((com.mojang.authlib.GameProfile) obj);
    }

    @Redirect(method = "getEntriesWithLimit", at = @At(value = "INVOKE", target = "Ljava/util/Deque;iterator()Ljava/util/Iterator;", remap = false))
    public Iterator<com.mojang.authlib.GameProfile> onGetEntriesWithLimit(Deque<com.mojang.authlib.GameProfile> list) {
        return this.profiles.iterator();
    }

    @Redirect(method = "load", at = @At(value = "INVOKE", target = "Ljava/util/Deque;clear()V", remap = false))
    public void onLoad(Deque<com.mojang.authlib.GameProfile> list) {
        this.profiles.clear();
    }

    @Redirect(method = "lookupProfile(Lcom/mojang/authlib/GameProfileRepository;Ljava/lang/String;)Lcom/mojang/authlib/GameProfile;",
            at = @At(value = "INVOKE", target = "Lcom/mojang/authlib/GameProfileRepository;findProfilesByNames([Ljava/lang/String;"
                    + "Lcom/mojang/authlib/Agent;Lcom/mojang/authlib/ProfileLookupCallback;)V", remap = false))
    private static void onGetGameProfile(GameProfileRepository repository, String[] names, Agent agent, ProfileLookupCallback callback) {
        GameProfileCache cache = null;
        try {
            cache = Sponge.getServer().getGameProfileManager().getCache();
        } catch (Throwable t) {
            // ignore
        }

        if (cache == null || cache instanceof PlayerProfileCache) {
            repository.findProfilesByNames(names, agent, callback);
        } else {
            // The method we're redirecting into obtains the resulting GameProfile from
            // the callback here.
            callback.onProfileLookupSucceeded((com.mojang.authlib.GameProfile) cache.getOrLookupByName(names[0]).orElse(null));
        }
    }

    @Inject(method = "save", at = @At("HEAD"), cancellable = true)
    public void onSave(CallbackInfo ci) {
        if (!this.canSave) {
            ci.cancel();
        }
    }

    @Override
    public boolean add(GameProfile profile, boolean overwrite, @Nullable Date expiry) {
        checkNotNull(profile, "profile");

        // Don't attempt to overwrite entries if we aren't requested to do so
        if (this.uuidToProfileEntryMap.containsKey(profile.getUniqueId()) && !overwrite) {
            return false;
        }

        this.addEntry((com.mojang.authlib.GameProfile) profile, expiry);

        return true;
    }

    @Override
    public boolean remove(GameProfile profile) {
        checkNotNull(profile, "profile");

        UUID uniqueId = profile.getUniqueId();

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
    public Collection<GameProfile> remove(Iterable<GameProfile> profiles) {
        checkNotNull(profiles, "profiles");

        Collection<GameProfile> result = Lists.newArrayList();

        for (GameProfile profile : profiles) {
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
        this.save();
    }

    @Override
    public Optional<GameProfile> getById(UUID uniqueId) {
        return Optional.ofNullable((GameProfile) this.getProfileByUUID(checkNotNull(uniqueId, "unique id")));
    }

    @Override
    public Map<UUID, Optional<GameProfile>> getByIds(Iterable<UUID> uniqueIds) {
        checkNotNull(uniqueIds, "unique ids");

        Map<UUID, Optional<GameProfile>> result = Maps.newHashMap();

        for (UUID uniqueId : uniqueIds) {
            result.put(uniqueId, Optional.ofNullable((GameProfile) this.getProfileByUUID(uniqueId)));
        }

        return result.isEmpty() ? ImmutableMap.of() : ImmutableMap.copyOf(result);
    }

    @Override
    public Optional<GameProfile> lookupById(UUID uniqueId) {
        checkNotNull(uniqueId, "unique id");

        com.mojang.authlib.GameProfile profile = SpongeImpl.getServer().getMinecraftSessionService().fillProfileProperties(
                new com.mojang.authlib.GameProfile(uniqueId, ""), true);
        if (profile != null && profile.getName() != null && !profile.getName().isEmpty()) {
            this.addEntry(profile, null);
            return Optional.of((GameProfile) profile);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Map<UUID, Optional<GameProfile>> lookupByIds(Iterable<UUID> uniqueIds) {
        checkNotNull(uniqueIds, "unique ids");

        Map<UUID, Optional<GameProfile>> result = Maps.newHashMap();

        MinecraftSessionService service = SpongeImpl.getServer().getMinecraftSessionService();
        for (UUID uniqueId : uniqueIds) {
            com.mojang.authlib.GameProfile profile = service.fillProfileProperties(new com.mojang.authlib.GameProfile(uniqueId, ""), true);
            if (profile != null && profile.getName() != null && !profile.getName().isEmpty()) {
                this.addEntry(profile, null);
                result.put(uniqueId, Optional.of((GameProfile) profile));
            } else {
                // create a dummy profile to avoid future lookups
                // if actual user logs in, the profile will be updated during PlayerList#initializeConnectionToPlayer
                this.addEntry(new com.mojang.authlib.GameProfile(uniqueId, "[sponge]"), null);
                result.put(uniqueId, Optional.empty());
            }
        }

        return result.isEmpty() ? ImmutableMap.of() : ImmutableMap.copyOf(result);
    }

    @Override
    public Optional<GameProfile> getOrLookupById(UUID uniqueId) {
        Optional<GameProfile> profile = this.getById(uniqueId);
        if (profile.isPresent()) {
            return profile;
        } else {
            return this.lookupById(uniqueId);
        }
    }

    @Override
    public Map<UUID, Optional<GameProfile>> getOrLookupByIds(Iterable<UUID> uniqueIds) {
        checkNotNull(uniqueIds, "unique ids");

        Collection<UUID> pending = Sets.newHashSet(uniqueIds);
        Map<UUID, Optional<GameProfile>> result = Maps.newHashMap();

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
    public Optional<GameProfile> getByName(String name) {
        return Optional.ofNullable((GameProfile) this.getByNameNoLookup(checkNotNull(name, "name")));
    }

    @Override
    public Map<String, Optional<GameProfile>> getByNames(Iterable<String> names) {
        checkNotNull(names, "names");

        Map<String, Optional<GameProfile>> result = Maps.newHashMap();

        for (String name : names) {
            result.put(name, Optional.ofNullable((GameProfile) this.getByNameNoLookup(name)));
        }

        return result.isEmpty() ? ImmutableMap.of() : ImmutableMap.copyOf(result);
    }

    @Override
    public Optional<GameProfile> lookupByName(String name) {
        SingleProfileLookupCallback callback = new SingleProfileLookupCallback();

        SpongeImpl.getServer().getGameProfileRepository().findProfilesByNames(new String[]{name}, Agent.MINECRAFT, callback);

        Optional<GameProfile> profile = callback.getResult();
        if (profile.isPresent()) {
            this.addEntry((com.mojang.authlib.GameProfile) profile.get(), null);
        }

        return profile;
    }

    @Override
    public Map<String, Optional<GameProfile>> lookupByNames(Iterable<String> names) {
        checkNotNull(names, "names");

        Map<String, Optional<GameProfile>> result = Maps.newHashMap();

        SpongeImpl.getServer().getGameProfileRepository().findProfilesByNames(Iterables.toArray(names, String.class), Agent.MINECRAFT,
                new MapProfileLookupCallback(result));

        if (!result.isEmpty()) {
            for (Optional<GameProfile> entry : result.values()) {
                if (entry.isPresent()) {
                    this.addEntry((com.mojang.authlib.GameProfile) entry.get(), null);
                }
            }
            return ImmutableMap.copyOf(result);
        } else {
            return ImmutableMap.of();
        }
    }

    @Override
    public Optional<GameProfile> getOrLookupByName(String name) {
        Optional<GameProfile> profile = this.getByName(name);
        if (profile.isPresent()) {
            return profile;
        } else {
            return this.lookupByName(name);
        }
    }

    @Override
    public Map<String, Optional<GameProfile>> getOrLookupByNames(Iterable<String> names) {
        checkNotNull(names, "names");

        Collection<String> pending = Sets.newHashSet(names);
        Map<String, Optional<GameProfile>> result = Maps.newHashMap();

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
    public Optional<GameProfile> fillProfile(GameProfile profile, boolean signed) {
        checkNotNull(profile, "profile");

        return Optional.ofNullable((GameProfile) SpongeImpl.getServer().getMinecraftSessionService()
                .fillProfileProperties((com.mojang.authlib.GameProfile) profile, signed));
    }

    @Override
    public Collection<GameProfile> getProfiles() {
        return this.usernameToProfileEntryMap.values().stream()
                .map(entry -> (GameProfile) entry.getGameProfile())
                .collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public Collection<GameProfile> match(String name) {
        final String search = checkNotNull(name, "name").toLowerCase(Locale.ROOT);

        return this.getProfiles().stream()
                .filter(profile -> profile.getName().isPresent())
                .filter(profile -> profile.getName().get().toLowerCase(Locale.ROOT).startsWith(search))
                .collect(ImmutableSet.toImmutableSet());
    }

    @Nullable
    public com.mojang.authlib.GameProfile getByNameNoLookup(String username) {
        @Nullable IMixinPlayerProfileCacheEntry entry = this.usernameToProfileEntryMap.get(username.toLowerCase(Locale.ROOT));

        if (entry != null && System.currentTimeMillis() >= entry.getExpirationDate().getTime()) {
            com.mojang.authlib.GameProfile profile = entry.getGameProfile();
            this.uuidToProfileEntryMap.remove(profile.getId());
            this.usernameToProfileEntryMap.remove(profile.getName().toLowerCase(Locale.ROOT));
            this.profiles.remove(profile);
            entry = null;
        }

        if (entry != null) {
            com.mojang.authlib.GameProfile profile = entry.getGameProfile();
            this.profiles.remove(profile);
            this.profiles.add(profile);
        }

        return entry == null ? null : entry.getGameProfile();
    }

    @Override
    public boolean canSave() {
        return this.canSave;
    }

    @Override
    public void setCanSave(boolean flag) {
        this.canSave = flag;
    }
}
