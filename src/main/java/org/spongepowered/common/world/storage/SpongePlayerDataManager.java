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
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Server;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.world.server.SpongeWorldManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

public final class SpongePlayerDataManager {

    private static final String SPONGE_DATA = "sponge";
    private final Server server;
    private final Path playersDirectory;

    public SpongePlayerDataManager(final Server server) {
        this.server = server;
        this.playersDirectory = ((SpongeWorldManager) this.server.worldManager()).getDefaultWorldDirectory().resolve("data").resolve(SpongePlayerDataManager.SPONGE_DATA);
    }

    public void readLegacyPlayerData(final ServerPlayer playerEntity, final CompoundTag compound, @Nullable Instant creation) {
        if (creation == null) {
            creation = Instant.now();
        }
        Instant lastPlayed = creation;
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
        final Path playerFile = this.playersDirectory.resolve(playerEntity.uniqueId() + ".dat");
        if (Files.isReadable(playerFile)) {
            final CompoundTag playerFileCompound;
            try (final InputStream stream = Files.newInputStream(playerFile)) {
                playerFileCompound = NbtIo.readCompressed(stream, NbtAccounter.unlimitedHeap());
                creation = Instant.ofEpochMilli(playerFileCompound.getLong(Constants.Sponge.PlayerData.PLAYER_DATA_JOIN.toString()));
                lastPlayed = Instant.ofEpochMilli(playerFileCompound.getLong(Constants.Sponge.PlayerData.PLAYER_DATA_LAST.toString()));
            } catch (final Exception e) {
                throw new RuntimeException("Failed to decompress playerdata for playerfile " + playerFile, e);
            }
        }
        playerEntity.offer(Keys.FIRST_DATE_JOINED, creation);
        playerEntity.offer(Keys.LAST_DATE_PLAYED, lastPlayed);
    }

    public void deleteLegacyPlayerData(final ServerPlayer playerEntity) {
        final Path playerFile = this.playersDirectory.resolve(playerEntity.uniqueId() + ".dat");
        if (Files.isRegularFile(playerFile)) {
            try {
                Files.delete(playerFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
