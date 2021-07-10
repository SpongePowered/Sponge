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
package org.spongepowered.common.user;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Server;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.GameProfileCache;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.server.players.PlayerListAccessor;
import org.spongepowered.common.accessor.world.level.storage.PlayerDataStorageAccessor;
import org.spongepowered.common.entity.player.SpongeUser;
import org.spongepowered.common.profile.SpongeGameProfile;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public final class ServerUserProvider {

    private final MinecraftServer server;

    // This is the important set - this tells us if a User file actually exists,
    // it should mirror the filesystem.
    private final Set<UUID> knownUUIDs = new HashSet<>();
    private final Cache<UUID, User> userCache;

    private final Map<String, MutableWatchEvent> watcherUpdateMap = new HashMap<>();

    private @Nullable WatchService filesystemWatchService = null;
    private @Nullable WatchKey watchKey = null;

    public ServerUserProvider(final Server server) {
        this.userCache = Caffeine.newBuilder()
                .expireAfterAccess(1, TimeUnit.DAYS)
                .build();
        this.server = (MinecraftServer) server;
    }

    void setupWatchers() {
        this.teardownWatchers();
        // Setup the watch service
        try {
            this.filesystemWatchService = FileSystems.getDefault().newWatchService();
            this.watchKey = this.getSaveHandlerDirectory().register(
                    this.filesystemWatchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE);
        } catch (final IOException e) {
            SpongeCommon.logger().warn("Could not start file watcher");
            if (this.filesystemWatchService != null) {
                // it might be the watchKey that failed, so null it out again.
                try {
                    this.filesystemWatchService.close();
                } catch (final IOException ex) {
                    // ignored
                }
            }
            this.watchKey = null;
            this.filesystemWatchService = null;
        }
    }

    void teardownWatchers() {
        if (this.watchKey != null) {
            this.watchKey.cancel();
            this.watchKey = null;
        }

        if (this.filesystemWatchService != null) {
            try {
                this.filesystemWatchService.close();
            } catch (final IOException e) {
                // ignored - we're nulling this anyway
            } finally {
                this.filesystemWatchService = null;
            }
        }
    }

    void refreshFilesystemProfiles() {
        if (this.watchKey != null && this.watchKey.isValid()) {
            this.watchKey.reset();
        }
        this.knownUUIDs.clear();
        this.userCache.invalidateAll();

        // Add all known profiles from the data files
        final String[] uuids = this.getSaveHandler().getSeenPlayers();
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
            } catch (final Exception ex) {
                continue;
            }

            this.knownUUIDs.add(uuid);
        }
    }

    Optional<User> getUser(final String lastKnownName) {
        final com.mojang.authlib.GameProfile gameProfile = this.server.getProfileCache().get(lastKnownName);
        if (gameProfile == null) {
            return Optional.empty();
        }
        return this.getUser(SpongeGameProfile.of(gameProfile));
    }

    Optional<User> getUser(final UUID uuid) {
        final com.mojang.authlib.GameProfile gameProfile = this.server.getProfileCache().get(uuid);
        return this.getUser(gameProfile == null ? null : SpongeGameProfile.of(gameProfile));
    }

    Optional<User> getUser(final @Nullable GameProfile profile) {
        this.pollFilesystemWatcher();
        if (profile != null && this.knownUUIDs.contains(profile.uniqueId())) {
            // This is okay, the file exists.
            return Optional.of(this.getOrCreateUser(profile, false));
        }
        return Optional.empty();
    }

    User getOrCreateUser(final GameProfile profile, final boolean force) {
        final com.mojang.authlib.GameProfile resolvedProfile;
        if (!force) {
            final UUID userID = profile.uniqueId();
            final User currentUser = this.userCache.getIfPresent(userID);
            if (currentUser != null) {
                return currentUser;
            }
            // ensure the profile is what we expect it to be
            final com.mojang.authlib.GameProfile p = this.server.getProfileCache().get(userID);
            resolvedProfile = p == null ? SpongeGameProfile.toMcProfile(profile) : p;
        } else {
            resolvedProfile = SpongeGameProfile.toMcProfile(profile);
            final User currentUser = this.userCache.getIfPresent(profile.uniqueId());
            if (currentUser != null) {
                if (SpongeUser.dirtyUsers.contains(currentUser)) {
                    ((SpongeUser) currentUser).save();
                    ((SpongeUser) currentUser).invalidate();
                }
            }
        }

        this.pollFilesystemWatcher();
        final User user = new SpongeUser(resolvedProfile);
        this.userCache.put(profile.uniqueId(), user);
        this.knownUUIDs.add(profile.uniqueId());
        return user;
    }

    boolean deleteUser(final UUID uuid) {
        if (this.deleteStoredPlayerData(uuid)) {
            this.userCache.invalidate(uuid);
            this.knownUUIDs.remove(uuid);
            return true;
        }
        return false;
    }

    Stream<GameProfile> matchKnownProfiles(final String lowercaseName) {
        return ((Server) this.server).gameProfileManager().cache().streamOfMatches(lowercaseName)
                .filter(gameProfile -> this.knownUUIDs.contains(gameProfile.uniqueId()));
    }

    Stream<GameProfile> streamAll() {
        final GameProfileCache cache = ((Server) this.server).gameProfileManager().cache();
        return this.knownUUIDs.stream().map(x -> cache.findById(x).orElseGet(() -> GameProfile.of(x)));
    }

    private Path getPlayerDataFile(final UUID uniqueId) {
        // Note: Uses the overworld's player data
        final Path file = this.getSaveHandlerDirectory().resolve(uniqueId.toString() + ".dat");
        //new File(saveHandler.accessor$getPlayersDirectory(), uniqueId.toString() + ".dat");
        if (Files.exists(file)) {
            return file;
        }
        return null;
    }

    private boolean deleteStoredPlayerData(final UUID uniqueId) {
        final Path dataFile = this.getPlayerDataFile(uniqueId);
        if (dataFile != null) {
            try {
                return Files.deleteIfExists(dataFile);
            } catch (final SecurityException | IOException e) {
                SpongeCommon.logger().warn("Unable to delete file {}", dataFile, e);
                return false;
            }
        }
        return true;
    }

    private void pollFilesystemWatcher() {
        if (this.watchKey == null || !this.watchKey.isValid()) {
            // Reboot this if it's somehow failed.
            this.refreshFilesystemProfiles();
            this.setupWatchers();
            return;
        }
        // We've already got the UUIDs, so we need to just see if the file system
        // watcher has found any more (or removed any).
        synchronized (this.watcherUpdateMap) {
            this.watcherUpdateMap.clear();
            for (final WatchEvent<?> event : this.watchKey.pollEvents()) {
                @SuppressWarnings("unchecked") final WatchEvent<Path> ev = (WatchEvent<Path>) event;
                final Path file = ev.context();

                // It is possible that the context is null, in which case, ignore it.
                if (file != null) {
                    final String filename = file.getFileName().toString();

                    // We don't determine the UUIDs yet, we'll only do that if we need to.
                    this.watcherUpdateMap.computeIfAbsent(filename, f -> new MutableWatchEvent()).set(ev.kind());
                }
            }

            // Now we know what the final result is, we can act upon it.
            for (final Map.Entry<String, MutableWatchEvent> entry : this.watcherUpdateMap.entrySet()) {
                final WatchEvent.Kind<?> kind = entry.getValue().get();
                if (kind != null) {
                    final String name = entry.getKey();
                    final UUID uuid;
                    if (name.endsWith(".dat")) {
                        try {
                            uuid = UUID.fromString(name.substring(0, name.length() - 4));

                            // It will only be create or delete here.
                            if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                                this.knownUUIDs.add(uuid);
                            } else {
                                this.knownUUIDs.remove(uuid);
                                // We don't do this, in case we were caught at a bad time.
                                // Everything else should handle it for us, however.
                                // this.userCache.invalidate(uuid);
                            }
                        } catch (final IllegalArgumentException ex) {
                            // ignored, file isn't of use to us.
                        }
                    }
                }
            }
        }
    }

    private PlayerDataStorage getSaveHandler() {
        return (PlayerDataStorage) ((PlayerListAccessor) this.server.getPlayerList()).accessor$playerIo();
    }

    private Path getSaveHandlerDirectory() {
        return ((PlayerDataStorageAccessor) this.getSaveHandler()).accessor$playerDir().toPath();
    }

    // Used to reduce the number of calls to maps.
    private static final class MutableWatchEvent {

        private WatchEvent.Kind<?> kind = null;

        public WatchEvent.Kind<?> get() {
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
