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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Server;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.GameProfileCache;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.api.profile.GameProfileProvider;
import org.spongepowered.common.SpongeServer;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;
import org.spongepowered.common.bridge.server.players.GameProfileCacheBridge;
import org.spongepowered.common.bridge.server.players.GameProfileCache_GameProfileInfoBridge;
import org.spongepowered.common.util.PrettyPrinter;
import org.spongepowered.common.util.UsernameCache;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class SpongeGameProfileManager implements GameProfileManager {

    private static final Logger LOGGER = LogManager.getLogger();

    private final UsernameCache usernameCache;
    private final GameProfileCacheBridge cache;
    private final UncachedGameProfileProvider uncached = new UncachedGameProfileProvider();
    private final ExecutorService gameLookupExecutorService;

    public SpongeGameProfileManager(final Server server) {
        this.usernameCache = ((SpongeServer) server).getUsernameCache();
        this.cache = (GameProfileCacheBridge) ((MinecraftServer) server).getProfileCache();
        this.gameLookupExecutorService = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
            .setNameFormat("Sponge - Async User Lookup Thread").build());
    }

    static boolean canLookup(final UUID id) {
        return id.version() == 4; // only v4 UUIDs are used by valid accounts -- everything else cannot return a response, so don't waste our time
    }

    @Override
    public GameProfileCache cache() {
        return this.cache;
    }

    @Override
    public GameProfileProvider uncached() {
        return this.uncached;
    }

    @Override
    public CompletableFuture<GameProfile> basicProfile(final UUID uniqueId) {
        Objects.requireNonNull(uniqueId, "uniqueId");
        final Optional<GameProfileCache_GameProfileInfoBridge> entry = this.cache.bridge$getEntry(uniqueId);
        if (entry.isPresent()) {
            return CompletableFuture.completedFuture(entry.get().bridge$getBasic());
        }
        final String cachedName = this.usernameCache.getLastKnownUsername(uniqueId);
        if (cachedName != null) {
            final GameProfile profile = new SpongeGameProfile(uniqueId, cachedName);
            this.cache.bridge$addBasic(profile);
            return CompletableFuture.completedFuture(profile);
        }
        return this.uncached().basicProfile(uniqueId).thenApply(profile -> {
            this.cache.bridge$addBasic(profile);
            return profile;
        });
    }

    @Override
    public CompletableFuture<GameProfile> basicProfile(final String name, final @Nullable Instant time) {
        Objects.requireNonNull(name, "name");
        if (time != null) {
            return this.uncached().basicProfile(name, time);
        }
        return this.cache.bridge$getEntry(name)
                .flatMap(entry -> Optional.ofNullable(entry.bridge$getBasic()))
                .map(CompletableFuture::completedFuture)
                .orElseGet(() -> this.uncached().basicProfile(name)
                        .thenApply(profile -> {
                            this.cache.bridge$addBasic(profile);
                            return profile;
                        }));
    }

    @Override
    public CompletableFuture<Map<String, GameProfile>> basicProfiles(final Iterable<String> names, final @Nullable Instant time) {
        Objects.requireNonNull(names, "names");
        if (time != null) {
            return this.uncached().basicProfiles(names, time);
        }
        final Map<String, GameProfile> result = new HashMap<>();
        final List<String> toLookup = new ArrayList<>();
        for (final String name : names) {
            final Optional<GameProfile> profile = this.cache.bridge$getEntry(name)
                    .flatMap(entry -> Optional.ofNullable(entry.bridge$getBasic()));
            if (profile.isPresent()) {
                result.put(name, profile.get());
            } else {
                toLookup.add(name);
            }
        }
        if (toLookup.isEmpty()) {
            return CompletableFuture.completedFuture(result);
        }
        return this.uncached().basicProfiles(toLookup).thenApply(lookedUp -> {
            for (final GameProfile profile : lookedUp.values()) {
                this.cache.bridge$addBasic(profile);
            }
            result.putAll(lookedUp);
            return result;
        });
    }

    @Override
    public CompletableFuture<GameProfile> profile(final String name, final boolean signed) {
        Objects.requireNonNull(name, "name");
        return this.cache.bridge$getEntry(name)
                .flatMap(entry -> Optional.ofNullable(entry.bridge$getFull(signed)))
                .map(CompletableFuture::completedFuture)
                .orElseGet(() -> this.uncached().profile(name, signed).thenApply(profile -> {
                    this.cache.bridge$add(profile, true, signed);
                    return profile;
                }));
    }

    @Override
    public CompletableFuture<GameProfile> profile(final UUID uniqueId, final boolean signed) {
        Objects.requireNonNull(uniqueId, "uniqueId");
        return this.cache.bridge$getEntry(uniqueId)
                .flatMap(entry -> Optional.ofNullable(entry.bridge$getFull(signed)))
                .map(CompletableFuture::completedFuture)
                .orElseGet(() -> this.uncached().profile(uniqueId, signed).thenApply(profile -> {
                    this.cache.bridge$add(profile, true, signed);
                    return profile;
                }));
    }

    public void lookupUserAsync(final UUID uniqueId) {
        Objects.requireNonNull(uniqueId, "uniqueId");
        this.gameLookupExecutorService.execute(() -> {
            try {
                this.basicProfile(uniqueId).get();
            } catch (final InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            if (SpongeGameProfileManager.canLookup(uniqueId)) { // only wait when it's possible that a lookup has actually occurred
                try {
                    Thread.sleep(SpongeConfigs.getCommon().get().world.gameProfileQueryTaskInterval * 1000L);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void close() {
        // Shut down the executor
        this.gameLookupExecutorService.shutdown();

        try {
            if (!this.gameLookupExecutorService.awaitTermination(5, TimeUnit.SECONDS)) {
                final List<Runnable> remaining = this.gameLookupExecutorService.shutdownNow();
                new PrettyPrinter()
                    .add("Sponge game profile resolver failed to shut down in 5 seconds!")
                    .add("Remaining tasks:")
                    .addWithIndices(remaining)
                    .add("These tasks have been cancelled.")
                    .log(SpongeGameProfileManager.LOGGER, Level.WARN);
            }
        } catch (final InterruptedException e) {
            SpongeGameProfileManager.LOGGER.error("The async scheduler was interrupted while awaiting shutdown!");
        }
    }
}
