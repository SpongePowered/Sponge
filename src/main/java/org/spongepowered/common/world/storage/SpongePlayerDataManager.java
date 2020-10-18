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

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import org.spongepowered.api.Server;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.data.persistence.NbtTranslator;
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

import javax.annotation.Nullable;

public final class SpongePlayerDataManager {

    private static final String SPONGE_DATA = "sponge";
    private final Server server;
    private final Map<UUID, SpongePlayerData> playerDataByUniqueId;
    private Path playersDirectory;

    public SpongePlayerDataManager(final Server server) {
        this.server = server;
        this.playerDataByUniqueId = new ConcurrentHashMap<>();
    }

    public void load() {
        try {
            this.playersDirectory = ((SpongeWorldManager) this.server.getWorldManager()).getSavesDirectory().resolve("data").resolve(SPONGE_DATA);
            Files.createDirectories(this.playersDirectory);

            final List<Path> playerFiles = new ArrayList<>();
            try (final DirectoryStream<Path> stream = Files.newDirectoryStream(this.playersDirectory, "*.{dat}")) {
                for (final Path entry : stream) {
                    playerFiles.add(entry);
                }
            } catch (DirectoryIteratorException e) {
                SpongeCommon.getLogger().error("Something happened when trying to gather all player files", e);
            }
            for (final Path playerFile : playerFiles) {
                if (Files.isReadable(playerFile)) {
                    final CompoundNBT compound;

                    try (final InputStream stream = Files.newInputStream(playerFile)) {
                        compound = CompressedStreamTools.readCompressed(stream);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to decompress playerdata for playerfile " + playerFile, e);
                    }

                    if (compound.isEmpty()) {
                        throw new RuntimeException("Failed to decompress player data within [" + playerFile + "]!");
                    }

                    final DataContainer container = NbtTranslator.getInstance().translateFrom(compound);
                    // TODO Minecraft 1.14 - Data Registration in the lifecycle
//                    final SpongePlayerData data = container.getSerializable(DataQuery.of(), SpongePlayerData.class).get();
//                    this.playerDataByUniqueId.put(data.uuid, data);
                }
            }
            playerFiles.clear();

        } catch (final Exception ex) {
            throw new RuntimeException("Encountered an exception while creating the player data handler!", ex);
        }
    }

    public void savePlayer(final UUID id) {
        @Nullable final SpongePlayerData data = this.playerDataByUniqueId.get(checkNotNull(id, "Player id cannot be null!"));
        if (data != null) {
            saveFile(id.toString(), createCompoundFor(data));
        } else {
            SpongeCommon.getLogger().error("Couldn't find a player data for the uuid: " + id.toString());
        }
    }

    private CompoundNBT createCompoundFor(final SpongePlayerData data) {
        return NbtTranslator.getInstance().translate(data.toContainer());
    }

    private void saveFile(final String id, final CompoundNBT compound) {
        try {
            // Ensure that where we want to put this at ALWAYS exists
            Files.createDirectories(this.playersDirectory);

            final Path finalDatPath = this.playersDirectory.resolve(id + ".dat");
            final Path newDatPath = this.playersDirectory.resolve(id + ".dat.tmp");
            try (final OutputStream stream = Files.newOutputStream(newDatPath, StandardOpenOption.CREATE)) {
                CompressedStreamTools.writeCompressed(compound, stream);
            }
            Files.move(newDatPath, finalDatPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            SpongeCommon.getLogger().error("Failed to save player data for [{}]!", id, e);
        }
    }

    public void setPlayerInfo(final UUID playerId, final Instant join, final Instant last) {
        checkNotNull(join, "Joined date cannot be null!");
        checkNotNull(last, "Last joined date cannot be null!");

        SpongePlayerData data = this.playerDataByUniqueId.get(checkNotNull(playerId, "Player UUID cannot be null!"));
        if (data == null) {
            data = new SpongePlayerData();
            data.uuid = playerId;
        }
        data.firstJoined = join.toEpochMilli();
        data.lastJoined = last.toEpochMilli();
        this.playerDataByUniqueId.put(playerId, data);
    }

    public Optional<Instant> getFirstJoined(final UUID uniqueId) {
        final SpongePlayerData data = this.playerDataByUniqueId.get(uniqueId);
        return Optional.ofNullable(data == null ? null : Instant.ofEpochMilli(data.firstJoined));
    }

    public Optional<Instant> getLastPlayed(final UUID uniqueId) {
        final SpongePlayerData data = this.playerDataByUniqueId.get(uniqueId);
        return Optional.ofNullable(data == null ? null : Instant.ofEpochMilli(data.lastJoined));
    }
}
