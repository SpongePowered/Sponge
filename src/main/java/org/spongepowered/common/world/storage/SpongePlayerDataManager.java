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
package org.spongepowered.common.world.storage;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Server;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.data.persistence.NBTTranslator;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.world.server.SpongeWorldManager;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SpongePlayerDataManager {

    private static final String SPONGE_DATA = "sponge";
    private final Server server;
    private final Map<UUID, SpongePlayerData> playerDataByUniqueId;
    @org.checkerframework.checker.nullness.qual.Nullable private Path playersDirectory = null;

    public SpongePlayerDataManager(final Server server) {
        this.server = server;
        this.playerDataByUniqueId = new ConcurrentHashMap<>();
    }

    public void load() {
        try {
            this.playersDirectory = ((SpongeWorldManager) this.server.worldManager()).getDefaultWorldDirectory().resolve("data").resolve(
                SpongePlayerDataManager.SPONGE_DATA);
            Files.createDirectories(this.playersDirectory);

            final List<Path> playerFiles = new ArrayList<>();
            try (final DirectoryStream<Path> stream = Files.newDirectoryStream(this.playersDirectory, "*.{dat}")) {
                for (final Path entry : stream) {
                    playerFiles.add(entry);
                }
            } catch (final DirectoryIteratorException e) {
                SpongeCommon.logger().error("Something happened when trying to gather all player files", e);
            }
            for (final Path playerFile : playerFiles) {
                if (Files.isReadable(playerFile)) {
                    final CompoundTag compound;

                    try (final InputStream stream = Files.newInputStream(playerFile)) {
                        compound = NbtIo.readCompressed(stream);
                    } catch (final Exception e) {
                        throw new RuntimeException("Failed to decompress playerdata for playerfile " + playerFile, e);
                    }

                    if (compound.isEmpty()) {
                        throw new RuntimeException("Failed to decompress player data within [" + playerFile + "]!");
                    }

                    final DataContainer container = NBTTranslator.INSTANCE.translateFrom(compound);
                    final SpongePlayerData data = container.getSerializable(DataQuery.of(), SpongePlayerData.class).get();
                    this.playerDataByUniqueId.put(data.getUniqueId(), data);
                }
            }
            playerFiles.clear();

        } catch (final Exception ex) {
            throw new RuntimeException("Encountered an exception while creating the player data handler!", ex);
        }
    }

    public void readPlayerData(final CompoundTag compound, @Nullable UUID playerUniqueId, @Nullable Instant creation) {
        if (creation == null) {
            creation = Instant.now();
        }
        Instant lastPlayed = Instant.now();
        // first try to migrate bukkit join data stuff
        if (compound.contains(Constants.Bukkit.BUKKIT, Constants.NBT.TAG_COMPOUND)) {
            final CompoundTag bukkitCompound = compound.getCompound(Constants.Bukkit.BUKKIT);
            creation = Instant.ofEpochMilli(bukkitCompound.getLong(Constants.Bukkit.BUKKIT_FIRST_PLAYED));
            lastPlayed = Instant.ofEpochMilli(bukkitCompound.getLong(Constants.Bukkit.BUKKIT_LAST_PLAYED));
        }
        // migrate canary join data
        if (compound.contains(Constants.Canary.ROOT, Constants.NBT.TAG_COMPOUND)) {
            final CompoundTag canaryCompound = compound.getCompound(Constants.Canary.ROOT);
            creation = Instant.ofEpochMilli(canaryCompound.getLong(Constants.Canary.FIRST_JOINED));
            lastPlayed = Instant.ofEpochMilli(canaryCompound.getLong(Constants.Canary.LAST_JOINED));
        }
        if (playerUniqueId == null) {
            if (compound.hasUUID(Constants.UUID)) {
                playerUniqueId = compound.getUUID(Constants.UUID);
            }
        }
        if (playerUniqueId != null) {
            final Optional<Instant> savedFirst = this.getFirstJoined(playerUniqueId);
            if (savedFirst.isPresent()) {
                creation = savedFirst.get();
            }
            final Optional<Instant> savedJoined = this.getLastPlayed(playerUniqueId);
            if (savedJoined.isPresent()) {
                lastPlayed = savedJoined.get();
            }
            this.setPlayerInfo(playerUniqueId, creation, lastPlayed);
        }
    }

    public void saveSpongePlayerData(final UUID uniqueId) {
        if (uniqueId == null) {
            throw new IllegalArgumentException("Player unique id cannot be null!");
        }

        final @Nullable SpongePlayerData data = this.playerDataByUniqueId.get(uniqueId);
        if (data != null) {
            this.saveFile(uniqueId.toString(), this.createCompoundFor(data));
        } else {
            SpongeCommon.logger().error("Couldn't find a player data for the uuid: " + uniqueId.toString());
        }
    }

    private CompoundTag createCompoundFor(final SpongePlayerData data) {
        return NBTTranslator.INSTANCE.translate(data.toContainer());
    }

    private void saveFile(final String id, final CompoundTag compound) {
        try {
            // Ensure that where we want to put this at ALWAYS exists
            Files.createDirectories(this.playersDirectory);

            final Path finalDatPath = this.playersDirectory.resolve(id + ".dat");
            final Path newDatPath = this.playersDirectory.resolve(id + ".dat.tmp");
            try (final OutputStream stream = Files.newOutputStream(newDatPath, StandardOpenOption.CREATE)) {
                NbtIo.writeCompressed(compound, stream);
            }
            Files.move(newDatPath, finalDatPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            SpongeCommon.logger().error("Failed to save player data for [{}]!", id, e);
        }
    }

    public void setPlayerInfo(final UUID playerUniqueId, final Instant join, final Instant last) {
        if (playerUniqueId == null) {
            throw new IllegalArgumentException("Player unique id cannot be null!");
        }
        if (join == null) {
            throw new IllegalArgumentException("Joined date cannot be null!");
        }
        if (last == null) {
            throw new IllegalArgumentException("Last joined date cannot be null!");
        }

        SpongePlayerData data = this.playerDataByUniqueId.get(playerUniqueId);
        if (data == null) {
            data = new SpongePlayerData();
            data.setUniqueId(playerUniqueId);
        }
        data.setFirstJoined(join.toEpochMilli());
        data.setLastJoined(last.toEpochMilli());
        this.playerDataByUniqueId.put(playerUniqueId, data);
    }

    public Optional<Instant> getFirstJoined(final UUID uniqueId) {
        final SpongePlayerData data = this.playerDataByUniqueId.get(uniqueId);
        return Optional.ofNullable(data == null ? null : Instant.ofEpochMilli(data.getFirstJoined()));
    }

    public Optional<Instant> getLastPlayed(final UUID uniqueId) {
        final SpongePlayerData data = this.playerDataByUniqueId.get(uniqueId);
        return Optional.ofNullable(data == null ? null : Instant.ofEpochMilli(data.getLastJoined()));
    }
}
