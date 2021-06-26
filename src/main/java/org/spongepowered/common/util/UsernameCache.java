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

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Server;
import org.spongepowered.common.SpongeCommon;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class UsernameCache {

    private static final Charset CHARSET = Charsets.UTF_8;

    private final Map<UUID, String> usernameByUniqueId;
    private final Gson gson;
    private final Path cacheFile;
    private boolean dirty = false;

    public UsernameCache(final Server server) {
        this.usernameByUniqueId = new ConcurrentHashMap<>();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.cacheFile = server.game().gameDirectory().resolve("usernamecache.json");
    }

    public void setUsername(final UUID uniqueId, final String username) {
        Preconditions.checkNotNull(uniqueId);
        Preconditions.checkNotNull(username);

        if (username.equals(this.usernameByUniqueId.get(uniqueId))) {
            return;
        }

        this.usernameByUniqueId.put(uniqueId, username);
        this.dirty = true;
    }

    public boolean removeUsername(final UUID uniqueId) {
        Preconditions.checkNotNull(uniqueId);

        if (this.usernameByUniqueId.remove(uniqueId) != null) {
            this.dirty = true;
            return true;
        }

        return false;
    }

    public @Nullable String getLastKnownUsername(final UUID uniqueId) {
        Preconditions.checkNotNull(uniqueId);

        return this.usernameByUniqueId.get(uniqueId);
    }

    public @Nullable UUID getLastKnownUUID(final String username) {
        Preconditions.checkNotNull(username);

        for (Map.Entry<UUID, String> mapEntry : this.usernameByUniqueId.entrySet()) {
            if (mapEntry.getValue().equalsIgnoreCase(username)) {
                return mapEntry.getKey();
            }
        }

        return null;
    }

    public boolean containsUUID(final UUID uniqueId) {
        Preconditions.checkNotNull(uniqueId);

        return this.usernameByUniqueId.containsKey(uniqueId);
    }

    public Map<UUID, String> getAll() {
        return Collections.unmodifiableMap(this.usernameByUniqueId);
    }

    public void load() {
        this.usernameByUniqueId.clear();

        if (Files.notExists(this.cacheFile)) {
            return;
        }

        try (final BufferedReader reader = Files.newBufferedReader(this.cacheFile, UsernameCache.CHARSET)) {
            final Type type = new TypeToken<Map<UUID, String>>() { private static final long serialVersionUID = 1L; }.getType();
            this.usernameByUniqueId.putAll(this.gson.fromJson(reader, type));
        } catch (final JsonSyntaxException e) {
            SpongeCommon.logger().error("Could not parse username cache file as valid json, deleting file", e);
            this.deleteCacheFile();
        } catch (final IOException e) {
            SpongeCommon.logger().error("Failed to read username cache file from disk, deleting file", e);
            this.deleteCacheFile();
        }
    }

    private void deleteCacheFile() {
        try {
            Files.deleteIfExists(this.cacheFile);
        } catch (IOException e) {
            SpongeCommon.logger().error("Failed to delete username cache file from disk!", e);
        }
    }

    public void save() {
        if (!this.dirty) {
            return;
        }

        try {
            // Make sure we don't save when another thread is still saving
            final String serialized = this.gson.toJson(this.usernameByUniqueId);
            Files.write(this.cacheFile, serialized.getBytes(UsernameCache.CHARSET));
            this.dirty = false;
        } catch (final IOException e) {
            SpongeCommon.logger().error("Failed to save username cache to file!", e);
        }
    }
}
