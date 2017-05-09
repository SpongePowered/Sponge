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
package org.spongepowered.common.util;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.spongepowered.common.SpongeImpl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

/**
 * Caches players last known usernames.
 *
 * <p>Modders should use {@link #getLastKnownUsername(UUID)} to determine
 * a players last known username.</p>
 *
 * <br>
 *
 * <p>For convenience, {@link #getMap()} is provided to get an immutable copy of
 * the caches underlying map.</p>
 * 
 * <p>Note: This class represents Forge's UsernameCache. It is used merely used
 * to support both SpongeForge and SpongeVanilla. Original code can be found
 * here :
 * 
 * https://github.com/MinecraftForge/MinecraftForge/blob/1.8.9/src/main/java/net/minecraftforge/common/UsernameCache.java
 * </p>
 */
public final class SpongeUsernameCache {

    // Thread-safe map
    private static Map<UUID, String> map = new ConcurrentHashMap<>();

    private static final Charset charset = Charsets.UTF_8;

    private static final File saveFile = new File(".", "usernamecache.json");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static boolean loaded = false;

    /**
     * Set a player's current username.
     *
     * @param uuid The player's {@link java.util.UUID UUID}
     * @param username The player's username
     */
    public static void setUsername(UUID uuid, String username) {
        checkNotNull(uuid);
        checkNotNull(username);
        if (!loaded) {
            load();
        }

        if (username.equals(map.get(uuid))) {
            return;
        }

        map.put(uuid, username);
    }

    /**
     * Remove a player's username from the cache.
     *
     * @param uuid The player's {@link java.util.UUID UUID}
     * @return If the cache contained the user
     */
    public static boolean removeUsername(UUID uuid) {
        checkNotNull(uuid);
        if (!loaded) {
            load();
        }

        if (map.remove(uuid) != null) {
            return true;
        }

        return false;
    }

    /**
     * Get the player's last known username.
     *
     * <p></p><b>May be <code>null</code></b></p>
     *
     * @param uuid The player's {@link java.util.UUID UUID}
     * @return The player's last known username, or <code>null</code> if the
     *     cache doesn't have a record of the last username
     */
    @Nullable
    public static String getLastKnownUsername(UUID uuid) {
        checkNotNull(uuid);
        if (!loaded) {
            load();
        }

        return map.get(uuid);
    }

    /**
     * Get the player's last known {@link java.util.UUID UUID}.
     *
     * <p><b>May be <code>null</code></b></p>
     *
     * @param username The player's username
     * @return the player's last known uuid, or <code>null</code> if the
     *     cache doesn't have a record of the username
     */
    @Nullable
    public static UUID getLastKnownUUID(String username) {
        checkNotNull(username);
        if (!loaded) {
            load();
        }

        for (Map.Entry<UUID, String> mapEntry : map.entrySet()) {
            if (mapEntry.getValue().equalsIgnoreCase(username)) {
                return mapEntry.getKey();
            }
        }

        return null;
    }

    /**
     * Check if the cache contains the given player's username.
     *
     * @param uuid The player's {@link java.util.UUID UUID}
     * @return If the cache contains a username for the given player
     */
    public static boolean containsUUID(UUID uuid) {
        checkNotNull(uuid);
        if (!loaded) {
            load();
        }

        return map.containsKey(uuid);
    }

    /**
     * Get an immutable copy of the cache's underlying map.
     *
     * @return The map
     */
    public static Map<UUID, String> getMap() {
        if (!loaded) {
            load();
        }

        return ImmutableMap.copyOf(map);
    }

    /**
     * Save the cache to file.
     */
    public static void save() {
        if (!loaded) {
            load();
        }

        try {
            // Make sure we don't save when another thread is still saving
            Files.write(gson.toJson(map), saveFile, charset);
        } catch (IOException e) {
            SpongeImpl.getLogger().error("Failed to save username cache to file!", e);
        }
    }

    /**
     * Load the cache from file.
     */
    public static void load() {
        loaded = true;
        if (!saveFile.exists()) {
            return;
        }

        try {
            String json = Files.toString(saveFile, charset);
            Type type = new TypeToken<Map<UUID, String>>() {
                private static final long serialVersionUID = 1L;
            }.getType();

            map = gson.fromJson(json, type);
        } catch (JsonSyntaxException e) {
            SpongeImpl.getLogger().error("Could not parse username cache file as valid json, deleting file", e);
            saveFile.delete();
        } catch (IOException e) {
            SpongeImpl.getLogger().error("Failed to read username cache file from disk, deleting file", e);
            saveFile.delete();
        } finally {
            // Can sometimes occur when the json file is malformed
            if (map == null) {
                map = Maps.newHashMap();
            }
        }
    }
}
