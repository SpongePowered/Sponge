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

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Lists;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.chunk.storage.RegionFile;
import net.minecraft.world.chunk.storage.RegionFileCache;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.util.Functional;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.world.chunk.storage.AnvilChunkLoaderBridge;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.mixin.core.world.chunk.storage.RegionFileCacheAccessor;
import org.spongepowered.common.util.Constants;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class WorldStorageUtil {

    public static CompletableFuture<Boolean> doesChunkExist(WorldServer world, IChunkLoader chunkLoader, Vector3i chunkCoords) {
        return doesChunkExist(world, chunkLoader, chunkCoords, SpongeImpl.getScheduler()::submitAsyncTask);
    }

    public static CompletableFuture<Boolean> doesChunkExistSync(WorldServer world, IChunkLoader chunkLoader, Vector3i chunkCoords) {
        return doesChunkExist(world, chunkLoader, chunkCoords, Functional::failableFuture);
    }

    public static CompletableFuture<Boolean> doesChunkExist(WorldServer world, IChunkLoader chunkLoader, Vector3i chunkCoords,
            Function<Callable<Boolean>, CompletableFuture<Boolean>> completableFutureProvider) {
        int x = chunkCoords.getX();
        int z = chunkCoords.getZ();
        if (!(chunkLoader instanceof AnvilChunkLoaderBridge) || !SpongeChunkLayout.instance.isValidChunk(x, chunkCoords.getY(), z)) {
            return CompletableFuture.completedFuture(false);
        }
        return completableFutureProvider.apply(() -> ((AnvilChunkLoaderBridge) chunkLoader).bridge$chunkExists(world, x, z));
    }

    public static CompletableFuture<Optional<DataContainer>> getChunkData(WorldServer world, IChunkLoader chunkLoader, Vector3i chunkCoords) {
        int x = chunkCoords.getX();
        int y = chunkCoords.getY();
        int z = chunkCoords.getZ();
        if (!(chunkLoader instanceof AnvilChunkLoaderBridge) || !SpongeChunkLayout.instance.isValidChunk(x, y, z)) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        File worldDir = ((AnvilChunkLoaderBridge) chunkLoader).bridge$getWorldDir().toFile();
        return SpongeImpl.getScheduler().submitAsyncTask(() -> {
            DataInputStream stream = RegionFileCache.func_76549_c(worldDir, x, z);
            return Optional.ofNullable(readDataFromRegion(stream));
        });
    }

    public static DataContainer readDataFromRegion(DataInputStream stream) throws IOException {
        if (stream == null) {
            return null;
        }
        NBTTagCompound data = CompressedStreamTools.func_74794_a(stream);

        // Checks are based on AnvilChunkLoader#checkedReadChunkFromNBT

        if (!data.func_150297_b(Constants.Chunk.CHUNK_DATA_LEVEL, Constants.NBT.TAG_COMPOUND)) {
            return null;
        }
        NBTTagCompound level = data.func_74775_l(Constants.Chunk.CHUNK_DATA_LEVEL);
        if (!level.func_150297_b(Constants.Chunk.CHUNK_DATA_SECTIONS, Constants.NBT.TAG_LIST)) {
            return null;
        }
        return NbtTranslator.getInstance().translateFrom(level);
    }

    public static Iterable<Path> listRegionFiles(Path worldDir) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(worldDir.resolve("region"), "*.mca")) {
            return Lists.newArrayList(stream);
        } catch (IOException e) {
            return Collections.emptySet();
        }
    }

    // Similar to RegionFileCache#createOrLoadRegionFile except this uses direct
    // file name instead of x,z
    public static RegionFile getRegionFile(Path regionFilePath) {
        File file = regionFilePath.toFile();
        RegionFile regionFile = RegionFileCacheAccessor.accessor$getRegionsByFileMap().get(file);
        if (regionFile != null) {
            return regionFile;
        }
        if (RegionFileCacheAccessor.accessor$getRegionsByFileMap().size() >= 256) {
            RegionFileCache.func_76551_a();
        }
        regionFile = new RegionFile(file);
        RegionFileCacheAccessor.accessor$getRegionsByFileMap().put(file, regionFile);
        return regionFile;
    }

}
