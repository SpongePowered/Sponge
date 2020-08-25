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
package org.spongepowered.common.service.user;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.server.management.UserListBans;
import net.minecraft.server.management.UserListBansEntry;
import net.minecraft.server.management.UserListEntry;
import net.minecraft.server.management.UserListWhitelist;
import net.minecraft.server.management.UserListWhitelistEntry;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.profile.GameProfileCache;
import org.spongepowered.api.profile.ProfileNotFoundException;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.entity.player.EntityPlayerMPBridge;
import org.spongepowered.common.bridge.world.storage.SaveHandlerBridge;
import org.spongepowered.common.entity.player.SpongeUser;
import org.spongepowered.common.mixin.core.server.management.UserLIstEntryAccessor;
import org.spongepowered.common.mixin.core.server.management.UserListAccessor;
import org.spongepowered.common.mixin.core.world.storage.SaveHandlerAccessor;
import org.spongepowered.common.world.WorldManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

class UserDiscoverer {

    private static final Map<String, MutableWatchEvent> updateCache = new HashMap<>();
    private static final Set<UUID> detectedStoredUUIDs = new HashSet<>();
    private static final Pattern DAT_FILENAME_SUFFIX = Pattern.compile("\\.dat$");

    @Nullable private static WatchService filesystemWatchService = null;
    @Nullable private static WatchKey watchKey = null;

    // Used to ensure that race conditions aren't hit when doing the filesystem checks
    private static final Object lockingObject = new Object();
    private static boolean hasInitBeenStarted = false;
    private static boolean scanningIO = true;

    // This is inherently tied to the user cache, so we use its removal listener to remove entries here.
    // Note that this cache is intended for _stored_ user data, while the GameProfileCache might contain
    // other user data. This is why we store it here.
    private static final Map<UUID, org.spongepowered.api.profile.GameProfile> gameProfileCache = new HashMap<>();
    private static final Multimap<String, User> caseInsensitiveUserByNameCache = HashMultimap.create();

    // It's possible for plugins to create 'fake users' with UserStorageService#getOrCreate,
    // whose names aren't registered with Mojang. To allow plugins to lookup
    // these users with UserStorageService#get, we need to cache users by name as well as by UUID
    private static final Cache<String, User> userByNameCache = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.DAYS)
            .build();

    private static final Cache<UUID, User> userCache = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.DAYS)
            .removalListener((RemovalNotification<UUID, User> removalNotification) -> {
                // If we're replacing, we're going through the create method so the other
                // caches will be updated accordingly.
                if (removalNotification.getCause() != RemovalCause.REPLACED) {
                    invalidateEntry(removalNotification.getValue().getProfile());
                } else {
                    // Just in case, we remove the entry from the name cache because that might not
                    // be updated
                    @Nullable String name = removalNotification.getValue().getName();
                    if (name != null) {
                        userByNameCache.invalidate(name);
                        caseInsensitiveUserByNameCache.remove(name.toLowerCase(), removalNotification.getValue());
                    }
                }
            })
            .build();


    // If a user doesn't exist, we should not put it into the cache, instead, we track it here.
    private static final Set<UUID> nonExistentUsers = new HashSet<>();

    static User create(final GameProfile profile) {
        User user = (User) new SpongeUser(profile);
        createCacheEntry(user);
        return user;
    }

    static User forceRecreate(final GameProfile profile) {
        final SpongeUser user = (SpongeUser) userCache.getIfPresent(profile.getId());
        if (user != null && SpongeUser.dirtyUsers.contains(user)) {
            user.save();
            user.invalidate(); // we're forcing the recreation so we need to ensure nothing will save to it now.
        }
        return create(profile);
    }

    /**
     * Searches for user data from a variety of places, in order of preference.
     * A user that has data in sponge may not necessarily have been online
     * before. A user added to the ban/whitelist that has not been on the server
     * before should be discoverable.
     *
     * @param profile The user's profile
     * @return The user data, or null if not found
     */
    @Nullable
    static User findByProfile(final org.spongepowered.api.profile.GameProfile profile) {
        final UUID uniqueId = profile.getUniqueId();
        User user = userCache.getIfPresent(uniqueId);
        if (user != null) {
            // update cached user with name
            if (user.getName() == null && profile.getName().isPresent()) {
                user = getFromStoredData(profile);
            }
            return user;
        }
        user = getOnlinePlayer(uniqueId);
        if (user != null) {
            nonExistentUsers.remove(profile.getUniqueId());
            return user;
        }
        user = getFromStoredData(profile);
        if (user != null) {
            return user;
        }
        user = getFromWhitelist(uniqueId);
        if (user != null) {
            return user;
        }
        user = getFromBanlist(uniqueId);
        return user;
    }

    @Nullable
    static User findByUsername(final String username) {
        final User user = userByNameCache.getIfPresent(username);
        if (user != null) {
            return user;
        }

        Collection<User> userCollection = caseInsensitiveUserByNameCache.get(username.toLowerCase());
        if (userCollection.size() == 1) {
            return userCollection.iterator().next();
        }

        // check mojang cache
        final PlayerProfileCache cache = SpongeImpl.getServer().getPlayerProfileCache();
        final HashSet<String> names = Sets.newHashSet(cache.getUsernames());
        if (names.contains(username.toLowerCase(Locale.ROOT))) {
            final GameProfile profile = cache.getGameProfileForUsername(username);
            if (profile != null) {
                return findByProfile((org.spongepowered.api.profile.GameProfile) profile);
            }
        }

        // check username cache
        final org.spongepowered.api.profile.GameProfile profile;
        try {
            profile = Sponge.getServer().getGameProfileManager().get(username).get();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while looking up username " + username, e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof ProfileNotFoundException) {
                return null;
            }
            throw new RuntimeException("Exception while looking up username " + username, e);
        }
        return UserDiscoverer.findByProfile(profile);
    }

    @SuppressWarnings("unchecked")
    static Collection<org.spongepowered.api.profile.GameProfile> getAllProfiles() {
        if (scanningIO) {
            // A good temporary measure is to use the game profile cache.
            return ((GameProfileCache) SpongeImpl.getServer().getPlayerProfileCache()).getProfiles();
        }

        Preconditions.checkState(Sponge.isServerAvailable(), "Server is not available!");
        if (filesystemWatchService == null || watchKey == null || !watchKey.isValid()) {
            startFilesystemWatchService();
            return ((GameProfileCache) SpongeImpl.getServer().getPlayerProfileCache()).getProfiles();
        }

        synchronized (lockingObject) {
            // Add all cached profiles to a new map, we don't want to alter the current "cache" map.
            final Map<UUID, org.spongepowered.api.profile.GameProfile> profiles = new HashMap<>(gameProfileCache);

            // Add all known profiles from the data files
            final PlayerProfileCache profileCache = SpongeImpl.getServer().getPlayerProfileCache();

            pollFilesystemWatcher();
            getProfilesFromDetectedUUIDs(profileCache, profiles);

            // Add all whitelisted users
            // Note: as the equality check in GameProfile requires both the UUID and name to be equal, we have to filter
            // out the game profiles by UUID only in the whitelist and ban list. If we don't, we end up with two
            // GameProfiles with the same UUID but different names, one of which is potentially invalid. For some
            // We assume that the cache is superior to the whitelist/banlist.
            //
            // See https://github.com/SpongePowered/SpongeCommon/issues/1989
            PlayerList pl = SpongeImpl.getServer().getPlayerList();
            addToProfiles(
                    ((UserListAccessor<GameProfile, UserListWhitelistEntry>) pl.getWhitelistedPlayers()).accessor$getValues().values(),
                    profiles,
                    profileCache);
            addToProfiles(
                    ((UserListAccessor<GameProfile, UserListBansEntry>) pl.getBannedPlayers()).accessor$getValues().values(),
                    profiles,
                    profileCache);
            return profiles.values();
        }
    }

    static void init() {
        if (!hasInitBeenStarted) {
            hasInitBeenStarted = true;
            startFilesystemWatchService();
        }
    }

    // Used to avoid blocking on a lock.
    private static void startFilesystemWatchService() {
        Preconditions.checkState(Sponge.isServerAvailable(), "Server is not available!");
        scanningIO = true;
        SpongeImpl.getScheduler().createAsyncExecutor(SpongeImpl.getPlugin())
                    .execute(UserDiscoverer::startFilesystemWatchServiceTask);
    }

    private static void startFilesystemWatchServiceTask() {
        // if we're in init, we need to remove the old service and watchkey
        synchronized (lockingObject) {
            if (watchKey != null) {
                watchKey.cancel();
                watchKey = null;
            }

            if (filesystemWatchService != null) {
                try {
                    filesystemWatchService.close();
                } catch (IOException e) {
                    // ignored - we're nulling this anyway
                } finally {
                    filesystemWatchService = null;
                }
            }

            detectedStoredUUIDs.clear();
            nonExistentUsers.clear();

            SaveHandlerBridge saveHandler = (SaveHandlerBridge) WorldManager.getWorldByDimensionId(0).get().getSaveHandler();
            Set<UUID> uuids = getAvailablePlayerUUIDs(saveHandler.bridge$getPlayersDirectory().toPath());
            detectedStoredUUIDs.addAll(uuids);

            // Anything we might have cached already, we should add it here,
            // in case it's been added but not saved yet
            detectedStoredUUIDs.addAll(userCache.asMap().keySet());

            // Setup the watch service
            try {
                filesystemWatchService = FileSystems.getDefault().newWatchService();
                watchKey = saveHandler.bridge$getPlayersDirectory().toPath().register(
                        filesystemWatchService,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE);
                scanningIO = false;
            } catch (IOException e) {
                SpongeImpl.getLogger().warn("Could not start file watcher");
                if (filesystemWatchService != null) {
                    // it might be the watchKey that failed, so null it out again.
                    try {
                        filesystemWatchService.close();
                    } catch (IOException ex) {
                        // ignored
                    }
                }
                watchKey = null;
                filesystemWatchService = null;
            }
        }
    }

    // This method is potentially slow and should be run as few times as possible (thus, the filesystem scan)
    private static Set<UUID> getAvailablePlayerUUIDs(Path playersDirectory) {
        Set<UUID> ret = new HashSet<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(playersDirectory, "*.dat")) {
            for (Path entry : stream) {
                try {
                    String name = DAT_FILENAME_SUFFIX.matcher(entry.getFileName().toString()).replaceAll("");
                    ret.add(UUID.fromString(name));
                } catch (IllegalArgumentException ex) {
                    // ignored - the file is not of interest to us
                }
            }
        } catch (IOException ex) {
            SpongeImpl.getLogger().error("Could not get the available UUIDs", ex);
        }

        return ret;
    }

    private static void getProfilesFromDetectedUUIDs(
            PlayerProfileCache profileCache,
            Map<UUID, org.spongepowered.api.profile.GameProfile> profiles) {

        for (UUID uuid : detectedStoredUUIDs) {
            final GameProfile profile = profileCache.getProfileByUUID(uuid);
            if (profile != null) {
                profiles.put(profile.getId(), (org.spongepowered.api.profile.GameProfile) profile);
            }
        }
    }

    private static void pollFilesystemWatcher() {
        // We've already got the UUIDs, so we need to just see if the file system
        // watcher has found any more (or removed any).
        synchronized (updateCache) {
            updateCache.clear(); // just in case
            for (final WatchEvent event : UserDiscoverer.watchKey.pollEvents()) {
                @SuppressWarnings("unchecked") final WatchEvent<Path> ev = (WatchEvent<Path>) event;
                final Path file = ev.context();
                if (file != null) {
                    final String filename = file.getFileName().toString();

                    // We don't determine the UUIDs yet, we'll only do that if we need to.
                    UserDiscoverer.updateCache.computeIfAbsent(filename, f -> new MutableWatchEvent()).set(ev.kind());
                }
            }

            for (Map.Entry<String, MutableWatchEvent> entry : updateCache.entrySet()) {
                @Nullable WatchEvent.Kind kind = entry.getValue().get();
                if (kind != null) {
                    String name = entry.getKey();
                    UUID uuid;
                    if (name.endsWith(".dat")) {
                        try {
                            uuid = UUID.fromString(name.substring(0, name.length() - 4));

                            // It will only be create or delete here.
                            if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                                detectedStoredUUIDs.add(uuid);
                            } else {
                                detectedStoredUUIDs.remove(uuid);
                            }
                        } catch (IllegalArgumentException ex) {
                            // ignored, file isn't of use to us.
                        }
                    }
                }
            }

            updateCache.clear();
        }
    }

    @SuppressWarnings("unchecked")
    private static void addToProfiles(
            final Collection<? extends UserListEntry<GameProfile>> gameProfiles,
            final Map<UUID, org.spongepowered.api.profile.GameProfile> profiles,
            final PlayerProfileCache profileCache) {

        gameProfiles.stream()
                .filter(x -> !profiles.containsKey(((UserLIstEntryAccessor<GameProfile>) x).accessor$getValue().getId()))
                .map(entry -> ((UserLIstEntryAccessor<GameProfile>) entry).accessor$getValue())
                .forEach(x -> {
                    // Get the known name, if it doesn't exist, then we don't add it - we assume no user backing
                    GameProfile profile = profileCache.getProfileByUUID(x.getId());
                    if (profile == null) {
                        // the name could be valid in this case (e.g. old ban that has dropped off the cache),
                        // so we add it to the Mojang cache
                        profile = x;
                        profileCache.addEntry(profile);
                    }

                    profiles.put(profile.getId(), (org.spongepowered.api.profile.GameProfile) profile);
                });
    }

    static boolean delete(final UUID uniqueId) {
        if (getOnlinePlayer(uniqueId) != null) {
            // Don't delete online player's data
            return false;
        }
        boolean success = deleteStoredPlayerData(uniqueId);
        success = success && deleteWhitelistEntry(uniqueId);
        success = success && deleteBanlistEntry(uniqueId);
        return success;
    }

    @Nullable
    private static User getOnlinePlayer(final UUID uniqueId) {
        Preconditions.checkState(Sponge.isServerAvailable(), "Server is not available!");
        final PlayerList playerList = SpongeImpl.getServer().getPlayerList();
        Preconditions.checkNotNull(playerList, "Server is not fully initialized yet! (Try a later event)");

        // Although the player itself could be returned here (as Player extends
        // User), a plugin is more likely to cache the User object and we don't
        // want the player entity to be cached.
        final EntityPlayerMPBridge player = (EntityPlayerMPBridge) playerList.getPlayerByUUID(uniqueId);
        if (player != null) {
            // If we're getting the online player, we want their current user,
            // rather than something that is recreated and may be out of sync
            // with the player itself, which is what #bridge$getUserObject does and was
            // the previous call here.
            //
            // Note: During initialization of the EntityPlayerMP, this method may
            // get called to set its User value. If so, the JLS section 4.12.5
            // states that the field will be null at this point, meaning
            // this will return an empty optional - rather than calling back into
            // itself and starting the cycle again. This might happen if a player's
            // User has dropped out of the cache above and the player is then recreated
            // through death or world teleport. This will prevent a stack overflow.
            final Optional<User> optional = player.bridge$getBackingUser();
            if (optional.isPresent()) {
                final User user = optional.get();
                createCacheEntry(user);
                return user;
            }
        }

        return null;
    }

    @Nullable
    private static User getFromStoredData(final org.spongepowered.api.profile.GameProfile profile) {
        // if we already saw there was no stored data, then there is no stored data!
        if (nonExistentUsers.contains(profile.getUniqueId())) {
            return null;
        }

        // Note: Uses the overworld's player data
        final File dataFile = getPlayerDataFile(profile.getUniqueId());
        if (dataFile == null) {
            // Tell the discoverer that we found nothing so we don't need to make this check in the future
            nonExistentUsers.add(profile.getUniqueId());
            return null;
        }

        // Create the user, this will cache it too.
        // Note: this was previously before the data file check. This had the unfortunate side effect of
        // creating a user when the user wasn't asked to be created (UserStorageService#get). The effect
        // was that the first get call would return Optional#empty, but the second would return a user
        // when a user wasn't expected to be created (that's what getOrCreate is for!)
        //
        // A call to create(GameProfile) will remove the profile UUID from nonExistentUsers, as the user
        // now exists!
        final User user = create((GameProfile) profile);

        return user;
    }

    @Nullable
    private static User getFromWhitelist(UUID uniqueId) {
        UserListWhitelist whiteList = SpongeImpl.getServer().getPlayerList().getWhitelistedPlayers();
        return getFromEntry(whiteList.getEntry(new GameProfile(uniqueId, "")));
    }

    @Nullable
    private static User getFromBanlist(UUID uniqueId) {
        UserListBans banList = SpongeImpl.getServer().getPlayerList().getBannedPlayers();
        return getFromEntry(banList.getEntry(new GameProfile(uniqueId, "")));
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static User getFromEntry(@Nullable UserListEntry<GameProfile> entry) {
        if (entry != null) {
            GameProfile profile = ((UserLIstEntryAccessor<GameProfile>) entry).accessor$getValue();
            if (profile != null) {
                return create(profile);
            }
        }

        return null;
    }

    @Nullable
    private static File getPlayerDataFile(final UUID uniqueId) {
        // This may be called triggered by mods using FakePlayer during
        // initial world gen (before the overworld is registered). Because of
        // this, we need to check if the overworld is actually registered yet
        final Optional<WorldServer> worldServer = WorldManager.getWorldByDimensionId(0);
        if (!worldServer.isPresent()) {
            return null;
        }

        // Note: Uses the overworld's player data
        final SaveHandlerAccessor saveHandler = (SaveHandlerAccessor) worldServer.get().getSaveHandler();
        final File file = new File(saveHandler.accessor$getPlayersDirectory(), uniqueId.toString() + ".dat");
        if (file.exists()) {
            return file;
        }
        return null;
    }

    private static boolean deleteStoredPlayerData(final UUID uniqueId) {
        final File dataFile = getPlayerDataFile(uniqueId);
        if (dataFile != null) {
            try {
                return dataFile.delete();
            } catch (SecurityException e) {
                SpongeImpl.getLogger().warn("Unable to delete file {} due to a security error", dataFile, e);
                return false;
            }
        }
        return true;
    }

    private static boolean deleteWhitelistEntry(final UUID uniqueId) {
        final UserListWhitelist whiteList = SpongeImpl.getServer().getPlayerList().getWhitelistedPlayers();
        whiteList.removeEntry(new GameProfile(uniqueId, ""));
        return true;
    }

    private static boolean deleteBanlistEntry(final UUID uniqueId) {
        final UserListBans banList = SpongeImpl.getServer().getPlayerList().getBannedPlayers();
        banList.removeEntry(new GameProfile(uniqueId, ""));
        return true;
    }

    private static void createCacheEntry(User user) {
        invalidateEntry(user.getProfile());
        userCache.put(user.getUniqueId(), user);
        gameProfileCache.put(user.getUniqueId(), user.getProfile());
        if (user.getName() != null) {
            userByNameCache.put(user.getName(), user);
            caseInsensitiveUserByNameCache.put(user.getName().toLowerCase(), user);
        }
        nonExistentUsers.remove(user.getUniqueId());
    }

    private static void invalidateEntry(org.spongepowered.api.profile.GameProfile profile) {
        UUID uuid = profile.getUniqueId();
        gameProfileCache.remove(uuid);
        userCache.invalidate(uuid);
        profile.getName().ifPresent(name -> {
            @Nullable User user = userByNameCache.getIfPresent(name);
            if (user != null && uuid.equals(user.getUniqueId())) {
                userByNameCache.invalidate(name);
                caseInsensitiveUserByNameCache.remove(user.getName().toLowerCase(), user);
            }
        });
    }

    // Used to reduce the number of calls to maps.
    static class MutableWatchEvent {

        @Nullable private WatchEvent.Kind<?> kind = null;

        @Nullable
        public WatchEvent.Kind get() {
            return this.kind;
        }

        public void set(WatchEvent.Kind<?> kind) {
            if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                // This should never happen, we don't listen to this.
                // However, if it does, treat it as a create, because it
                // infers the existence of the file.
                kind = StandardWatchEventKinds.ENTRY_CREATE;
            }

            if (kind == StandardWatchEventKinds.ENTRY_CREATE || kind == StandardWatchEventKinds.ENTRY_DELETE) {
                if (this.kind != null && this.kind != kind) {
                    this.kind = null;
                } else {
                    this.kind = kind;
                }
            }
        }

    }

}
