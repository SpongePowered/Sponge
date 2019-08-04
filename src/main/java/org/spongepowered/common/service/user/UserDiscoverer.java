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
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.server.management.UserListBans;
import net.minecraft.server.management.UserListEntry;
import net.minecraft.server.management.UserListEntryBan;
import net.minecraft.server.management.UserListWhitelist;
import net.minecraft.server.management.UserListWhitelistEntry;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.SaveHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.profile.ProfileNotFoundException;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.player.SpongeUser;
import org.spongepowered.common.bridge.entity.player.EntityPlayerMPBridge;
import org.spongepowered.common.mixin.core.server.management.UserLIstEntryAccessor;
import org.spongepowered.common.mixin.core.server.management.UserListAccessor;
import org.spongepowered.common.mixin.core.world.storage.SaveHandlerAccessor;
import org.spongepowered.common.world.WorldManager;

import java.io.File;
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

class UserDiscoverer {

    private static final Cache<UUID, User> userCache = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.DAYS)
            .build();

    // It's possible for plugins to create 'fake users' with UserStorageService#getOrCreate,
    // whose names aren't registered with Mojang. To allow plugins to lookup
    // these users with UserStorageService#get, we need to cache users by name as well as by UUID
    private static final Cache<String, User> userByNameCache = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.DAYS)
            .build();

    // If a user doesn't exist, we should not put it into the cache, instead, we track it here.
    private static final Set<UUID> nonExistentUsers = new HashSet<>();

    static User create(final GameProfile profile) {
        final User user = (User) new SpongeUser(profile);
        userCache.put(profile.getId(), user);
        if (profile.getName() != null) {
            userByNameCache.put(profile.getName(), user);
        }
        nonExistentUsers.remove(profile.getId());
        return user;
    }

    static User forceRecreate(final GameProfile profile) {
        final SpongeUser user = (SpongeUser) userCache.getIfPresent(profile.getId());
        if (user != null && SpongeUser.dirtyUsers.contains(user)) {
            user.save();
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

    static User findByUsername(final String username) {
        final User user = userByNameCache.getIfPresent(username);
        if (user != null) {
            return user;
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
        Preconditions.checkState(Sponge.isServerAvailable(), "Server is not available!");
        final Map<UUID, org.spongepowered.api.profile.GameProfile> profiles = new HashMap<>();

        // Add all cached profiles
        userCache.asMap().values().stream().map(User::getProfile).forEach(p -> profiles.put(p.getUniqueId(), p));

        // Add all known profiles from the data files
        final SaveHandler saveHandler = (SaveHandler) WorldManager.getWorldByDimensionId(0).get().getSaveHandler();
        final String[] uuids = saveHandler.getAvailablePlayerDat();
        final PlayerProfileCache profileCache = SpongeImpl.getServer().getPlayerProfileCache();
        for (final String playerUuid : uuids) {

            // If the filename contains a period, we can fail fast. Vanilla code fixes the Strings that have ".dat" to strip that out
            // before passing that back in getAvailablePlayerDat. It doesn't remove non ".dat" filenames from the list.
            if (playerUuid.contains(".")) {
                continue;
            }

            // At this point, we have a filename who has no extension. This doesn't mean it is actually a UUID. We trap the exception and ignore
            // any filenames that fail the UUID check.
            final UUID uuid;
            try {
                uuid = UUID.fromString(playerUuid);
            } catch (Exception ex) {
                continue;
            }

            // it exists, so we make sure to remove the uuid from the map (it may have been added manually in the meantime)
            nonExistentUsers.remove(uuid);
            final GameProfile profile = profileCache.getProfileByUUID(uuid);
            if (profile != null) {
                profiles.put(profile.getId(), (org.spongepowered.api.profile.GameProfile) profile);
            }
        }

        // Add all whitelisted users
        // Note: as the equality check in GameProfile requires both the UUID and name to be equal, we have to filter
        // out the game profiles by UUID only in the whitelist and ban list. If we don't, we end up with two GameProfiles
        // with the same UUID but different names, one of which is potentially invalid. For some
        // We assume that the cache is superior to the whitelist/banlist.
        //
        // See https://github.com/SpongePowered/SpongeCommon/issues/1989
        addToProfiles(((UserListAccessor<GameProfile, UserListWhitelistEntry>) SpongeImpl.getServer().getPlayerList().getWhitelistedPlayers()).accessor$getValues().values(), profiles, profileCache);
        addToProfiles(((UserListAccessor<GameProfile, UserListWhitelistEntry>) SpongeImpl.getServer().getPlayerList().getWhitelistedPlayers()).accessor$getValues().values(), profiles, profileCache);
        return profiles.values();
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
                userCache.put(uniqueId, user);
                userByNameCache.put(user.getName(), user);
                return user;
            }
        }

        return null;
    }

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

    @SuppressWarnings("unchecked")
    private static User getFromWhitelist(final UUID uniqueId) {
        GameProfile profile = null;
        final UserListWhitelist whiteList = SpongeImpl.getServer().getPlayerList().getWhitelistedPlayers();
        final UserListWhitelistEntry whiteListData = whiteList.getEntry(new GameProfile(uniqueId, ""));
        if (whiteListData != null) {
            profile = ((UserLIstEntryAccessor<GameProfile>) whiteListData).accessor$getValue();
        }
        if (profile != null) {
            return create(profile);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static User getFromBanlist(final UUID uniqueId) {
        GameProfile profile = null;
        final UserListBans banList = SpongeImpl.getServer().getPlayerList().getBannedPlayers();
        final UserListEntryBan<GameProfile> banData = banList.getEntry(new GameProfile(uniqueId, ""));
        if (banData != null) {
            profile = ((UserLIstEntryAccessor<GameProfile>) banData).accessor$getValue();
        }
        if (profile != null) {
            return create(profile);
        }
        return null;
    }

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

}
