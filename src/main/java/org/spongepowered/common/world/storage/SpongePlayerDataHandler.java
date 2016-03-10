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
import static com.google.common.base.Preconditions.checkState;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.world.DimensionManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

public final class SpongePlayerDataHandler {

    private static final String SPONGE_DATA = "sponge";
    private boolean hasInitialized = false;
    private Path playerDir;

    private final Map<UUID, SpongePlayerData> playerDataMap = new ConcurrentHashMap<>();

    public static void init() {
        SpongePlayerDataHandler handlerInstance = Holder.INSTANCE;
        if (handlerInstance.hasInitialized) {
            return;
        }
        @Nullable File root = DimensionManager.getCurrentSaveRootDirectory();
        if (root == null) {
            return;
        }
        final Path directoryRoot = root.toPath();
        if (root != null) { // ok, we're on the server, guaranteed.
            final String filePath = directoryRoot.toString() + File.separator + "data" + File.separator + SPONGE_DATA;
            try {
                handlerInstance.playerDir = Paths.get(filePath);
                Path file = Files.createDirectories(handlerInstance.playerDir);
                List<Path> playerFiles = new ArrayList<>();
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(file, "*.{dat}")) {
                    for (Path entry : stream) {
                        playerFiles.add(entry);
                    }
                } catch (DirectoryIteratorException e) {
                    SpongeImpl.getLogger().log(Level.ERROR, "Something happened when trying to gather all player files", e);
                }
                for (Path playerFile : playerFiles) {
                    File actualFile = playerFile.toFile();
                    if (actualFile.exists() && actualFile.isFile()) {
                        FileInputStream stream = new FileInputStream(actualFile);
                        NBTTagCompound compound = CompressedStreamTools.readCompressed(stream);
                        stream.close();
                        DataContainer container = NbtTranslator.getInstance().translateFrom(compound);
                        SpongePlayerData data = container.getSerializable(DataQuery.of(), SpongePlayerData.class).get();
                        handlerInstance.playerDataMap.put(data.uuid, data);
                    }
                }
                playerFiles.clear();

            } catch (FileAlreadyExistsException e) {
                SpongeImpl.getLogger().printf(Level.ERROR, "Someone went and created a file for the desired path: {}", filePath);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        handlerInstance.hasInitialized = true;
    }

    public static void savePlayer(UUID id) {
        checkState(Holder.INSTANCE.hasInitialized, "PlayerDataHandler hasn't initialized yet!");
        SpongePlayerDataHandler instance = Holder.INSTANCE;
        @Nullable SpongePlayerData data = instance.playerDataMap.get(checkNotNull(id, "Player id cannot be null!"));
        if (data != null) {
            saveFile(id.toString(), createCompoundFor(data));
        } else {
            SpongeImpl.getLogger().error("Couldn't find a player data for the uuid: " + id.toString());
        }
    }

    public static void saveFiles() {
        checkState(Holder.INSTANCE.hasInitialized, "PlayerDataHandler hasn't initialized yet!");
        SpongePlayerDataHandler instance = Holder.INSTANCE;
        Map<String, NBTTagCompound> compoundMap = new HashMap<>();
        for (Map.Entry<UUID, SpongePlayerData> entry : instance.playerDataMap.entrySet()) {
            SpongePlayerData data = entry.getValue();
            compoundMap.put(entry.getKey().toString(), createCompoundFor(data));
        }
        for (Map.Entry<String, NBTTagCompound> entry : compoundMap.entrySet()) {
            saveFile(entry.getKey(), entry.getValue());
        }
        compoundMap.clear();
    }

    private static NBTTagCompound createCompoundFor(SpongePlayerData data) {
        return NbtTranslator.getInstance().translateData(data.toContainer());
    }

    private static void saveFile(String id, NBTTagCompound compound) {
        checkState(Holder.INSTANCE.hasInitialized, "PlayerDataHandler hasn't initialized yet!");
        SpongePlayerDataHandler instance = Holder.INSTANCE;
        try {
            final String pathString = instance.playerDir.toString() + File.separator + id;
            File newFile = new File(pathString + "_new.dat");
            File finalFile = new File(pathString + ".dat");
            try (FileOutputStream stream = new FileOutputStream(newFile)) {
                CompressedStreamTools.writeCompressed(compound, stream);
                if (finalFile.exists()) {
                    if (!finalFile.delete()) {
                        SpongeImpl.getLogger().log(Level.WARN, "There was an issue deleting the previous file: " + finalFile.getAbsolutePath());
                    }
                }
                if (!newFile.renameTo(finalFile)) {
                    SpongeImpl.getLogger().error("Could not rename file: {} to {} !", newFile.getAbsolutePath(), finalFile.getAbsolutePath());
                }
                if (newFile.exists()) {
                    if (!newFile.delete()) {
                        SpongeImpl.getLogger().error("Could not delete file: {}", newFile.getAbsolutePath());
                    }
                }
            }
        } catch (Exception e) {
            SpongeImpl.getLogger().error("Failed to save player data: " + id);
            e.printStackTrace();
        }
    }

    public static void setPlayerInfo(UUID playerId, Instant join, Instant last) {
        checkState(Holder.INSTANCE.hasInitialized, "PlayerDataHandler hasn't initialized yet!");
        checkNotNull(join, "Joined date cannot be null!");
        checkNotNull(last, "Last joined date cannot be null!");
        SpongePlayerDataHandler instance = Holder.INSTANCE;

        SpongePlayerData data = instance.playerDataMap.get(checkNotNull(playerId, "Player UUID cannot be null!"));
        if (data == null) {
            data = new SpongePlayerData();
            data.uuid = playerId;
        }
        data.firstJoined = join.toEpochMilli();
        data.lastJoined = last.toEpochMilli();
        instance.playerDataMap.put(playerId, data);
    }

    public static Optional<Instant> getFirstJoined(UUID player) {
        checkState(Holder.INSTANCE.hasInitialized, "PlayerDataHandler hasn't initialized yet!");
        final SpongePlayerData data = Holder.INSTANCE.playerDataMap.get(player);
        return Optional.ofNullable(data == null ? null : Instant.ofEpochMilli(data.firstJoined));
    }

    public static Optional<Instant> getLastPlayed(UUID player) {
        checkState(Holder.INSTANCE.hasInitialized, "PlayerDataHandler hasn't initialized yet!");
        final SpongePlayerData data = Holder.INSTANCE.playerDataMap.get(player);
        return Optional.ofNullable(data == null ? null : Instant.ofEpochMilli(data.lastJoined));
    }

    private SpongePlayerDataHandler() { }

    private static final class Holder {
        static final SpongePlayerDataHandler INSTANCE = new SpongePlayerDataHandler();
    }
}
