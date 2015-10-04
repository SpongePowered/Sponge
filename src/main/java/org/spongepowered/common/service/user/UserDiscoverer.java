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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.BanEntry;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.server.management.UserListBans;
import net.minecraft.server.management.UserListBansEntry;
import net.minecraft.server.management.UserListWhitelist;
import net.minecraft.server.management.UserListWhitelistEntry;
import net.minecraft.world.storage.SaveHandler;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.common.entity.player.SpongeUser;
import org.spongepowered.common.interfaces.IMixinEntityPlayerMP;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.world.DimensionManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

class UserDiscoverer {

    private static final Map<UUID, User> userCache = Maps.newHashMap();

    static User create(GameProfile profile) {
        User user = (User) new SpongeUser(profile);
        userCache.put(profile.getId(), user);
        return user;
    }

    /**
     * Searches for user data from a variety of places, in order of preference.
     * A user that has data in sponge may not necessarily have been online
     * before. A user added to the ban/whitelist that has not been on the server
     * before should be discover-able.
     *
     * @param uniqueId The user's UUID
     * @return The user data, or null if not found
     */
    static User findByUuid(UUID uniqueId) {
        User user = userCache.get(uniqueId);
        if (user != null) {
            return user;
        }
        user = getOnlinePlayer(uniqueId);
        if (user != null) {
            return user;
        }
        user = getFromStoredData(uniqueId);
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

    static User findByUsername(String username) {
        PlayerProfileCache cache = MinecraftServer.getServer().getPlayerProfileCache();
        HashSet<String> names = Sets.newHashSet(cache.getUsernames());
        if (names.contains(username.toLowerCase(Locale.ROOT))) {
            GameProfile profile = cache.getGameProfileForUsername(username);
            if (profile != null) {
                return findByUuid(profile.getId());
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    static Collection<org.spongepowered.api.GameProfile> getAllProfiles() {
        Set<org.spongepowered.api.GameProfile> profiles = Sets.newHashSet();

        // Add all cached profiles
        for (User user : userCache.values()) {
            profiles.add(user.getProfile());
        }

        // Add all known profiles from the data files
        SaveHandler saveHandler = (SaveHandler) DimensionManager.getWorldFromDimId(0).getSaveHandler();
        String[] uuids = saveHandler.getAvailablePlayerDat();
        for (String playerUuid : uuids) {
            GameProfile profile = MinecraftServer.getServer().getPlayerProfileCache().getProfileByUUID(UUID.fromString(playerUuid));
            if (profile != null) {
                profiles.add((org.spongepowered.api.GameProfile) profile);
            }
        }

        // Add all whitelisted users
        UserListWhitelist whiteList = MinecraftServer.getServer().getConfigurationManager().getWhitelistedPlayers();
        for (UserListWhitelistEntry entry : (Collection<UserListWhitelistEntry>) whiteList.getValues().values()) {
            profiles.add((org.spongepowered.api.GameProfile) entry.value);
        }

        // Add all banned users
        UserListBans banList = MinecraftServer.getServer().getConfigurationManager().getBannedPlayers();
        for (BanEntry entry : (Collection<BanEntry>) banList.getValues().values()) {
            if (entry instanceof UserListBansEntry) {
                profiles.add((org.spongepowered.api.GameProfile) entry.value);
            }
        }
        return profiles;
    }

    static boolean delete(UUID uniqueId) {
        if (getOnlinePlayer(uniqueId) != null) {
            // Don't delete online player's data
            return false;
        }
        boolean success = deleteStoredPlayerData(uniqueId);
        success = success && deleteWhitelistEntry(uniqueId);
        success = success && deleteBanlistEntry(uniqueId);
        return success;
    }

    private static User getOnlinePlayer(UUID uniqueId) {
        ServerConfigurationManager confMgr = MinecraftServer.getServer().getConfigurationManager();
        if (confMgr == null) { // Server not started yet
            return null;
        }
        // Although the player itself could be returned here (as Player extends
        // User), a plugin is more likely to cache the User object and we don't
        // want the player entity to be cached.
        IMixinEntityPlayerMP player = (IMixinEntityPlayerMP) confMgr.getPlayerByUUID(uniqueId);
        if (player != null) {
            return player.getUserObject();
        }
        return null;
    }

    private static User getFromStoredData(UUID uniqueId) {
        // Note: Uses the overworld's player data
        File dataFile = getPlayerDataFile(uniqueId);
        if (dataFile == null) {
            return null;
        }
        GameProfile profile = MinecraftServer.getServer().getPlayerProfileCache().getProfileByUUID(uniqueId);
        if (profile != null) {
            User user = create(profile);
            try {
                ((SpongeUser) user).readFromNbt(CompressedStreamTools.readCompressed(new FileInputStream(dataFile)));
            } catch (IOException e) {
                SpongeHooks.logWarning("Corrupt user file {}. {}", dataFile, e);
            }
            return user;
        } else {
            return null;
        }
    }

    private static User getFromWhitelist(UUID uniqueId) {
        GameProfile profile = null;
        UserListWhitelist whiteList = MinecraftServer.getServer().getConfigurationManager().getWhitelistedPlayers();
        UserListWhitelistEntry whiteListData = (UserListWhitelistEntry) whiteList.getEntry(new GameProfile(uniqueId, ""));
        if (whiteListData != null) {
            profile = (GameProfile) whiteListData.value;
        }
        if (profile != null) {
            return create(profile);
        }
        return null;
    }

    private static User getFromBanlist(UUID uniqueId) {
        GameProfile profile = null;
        UserListBans banList = MinecraftServer.getServer().getConfigurationManager().getBannedPlayers();
        BanEntry banData = (BanEntry) banList.getEntry(new GameProfile(uniqueId, ""));
        if (banData instanceof UserListBansEntry) {
            profile = (GameProfile) banData.value;
        }
        if (profile != null) {
            return create(profile);
        }
        return null;
    }

    private static File getPlayerDataFile(UUID uniqueId) {
        // Note: Uses the overworld's player data
        SaveHandler saveHandler = (SaveHandler) DimensionManager.getWorldFromDimId(0).getSaveHandler();
        String[] uuids = saveHandler.getAvailablePlayerDat();
        for (String playerUuid : uuids) {
            if (uniqueId.toString().equals(playerUuid)) {
                return new File(saveHandler.playersDirectory, playerUuid + ".dat");
            }
        }
        return null;
    }

    private static boolean deleteStoredPlayerData(UUID uniqueId) {
        File dataFile = getPlayerDataFile(uniqueId);
        if (dataFile != null) {
            try {
                return dataFile.delete();
            } catch (SecurityException e) {
                SpongeHooks.logWarning("Unable to delete file {} due to a security error. {}", dataFile, e);
                return false;
            }
        }
        return true;
    }

    private static boolean deleteWhitelistEntry(UUID uniqueId) {
        UserListWhitelist whiteList = MinecraftServer.getServer().getConfigurationManager().getWhitelistedPlayers();
        whiteList.removeEntry(new GameProfile(uniqueId, ""));
        return true;
    }

    private static boolean deleteBanlistEntry(UUID uniqueId) {
        UserListBans banList = MinecraftServer.getServer().getConfigurationManager().getBannedPlayers();
        banList.removeEntry(new GameProfile(uniqueId, ""));
        return true;
    }

}
