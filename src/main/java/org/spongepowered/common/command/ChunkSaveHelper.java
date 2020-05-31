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
package org.spongepowered.common.command;

import com.flowpowered.math.GenericMath;
import com.google.gson.stream.JsonWriter;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.mixin.core.world.storage.SaveHandlerAccessor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class ChunkSaveHelper {

    @SuppressWarnings("rawtypes")
    public static void writeChunks(final File file, final boolean logAll) {
        try {
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }

            try (final JsonWriter writer = new JsonWriter(new FileWriter(file))) {
                writer.setIndent("  ");
                writer.beginArray();

                for (final World spongeWorld : SpongeImpl.getGame().getServer().getWorlds()) {
                    final WorldServer world = (WorldServer) spongeWorld;
                    writer.beginObject();
                    writer.name("name").value(((SaveHandlerAccessor) ((WorldServer) spongeWorld).getSaveHandler()).accessor$getSaveDirectoryName());
                    writer.name("dimensionId").value(((WorldServerBridge) spongeWorld).bridge$getDimensionId());
                    writer.name("players").value(world.playerEntities.size());
                    writer.name("loadedChunks").value(world.getChunkProvider().getLoadedChunks().size());
                    writer.name("activeChunks").value(world.getChunkProvider().getLoadedChunkCount());
                    writer.name("entities").value(world.loadedEntityList.size());
                    writer.name("tiles").value(world.loadedTileEntityList.size());

                    final Object2IntMap<ChunkPos> chunkEntityCounts = new Object2IntOpenHashMap<>();
                    chunkEntityCounts.defaultReturnValue(0);
                    final Reference2IntMap<Class> classEntityCounts = new Reference2IntOpenHashMap<>();
                    classEntityCounts.defaultReturnValue(0);
                    final Object2IntMap<Entity> entityCollisionCounts = new Object2IntOpenHashMap<>();
                    final Set<BlockPos> collidingCoords = new HashSet<>();
                    for (int i = 0; i < world.loadedEntityList.size(); i++) {
                        final Entity entity = world.loadedEntityList.get(i);
                        final ChunkPos chunkCoords = new ChunkPos((int) entity.posX >> 4, (int) entity.posZ >> 4);
                        chunkEntityCounts.put(chunkCoords, chunkEntityCounts.getInt(chunkCoords) + 1);
                        classEntityCounts.put(entity.getClass(), classEntityCounts.getInt(entity.getClass()) + 1);
                        if ((entity.getCollisionBoundingBox() != null) && logAll) {
                            final BlockPos coords =
                                    new BlockPos(GenericMath.floor(entity.posX), GenericMath.floor(entity.posY), GenericMath.floor(entity.posZ));
                            if (!collidingCoords.contains(coords)) {
                                collidingCoords.add(coords);
                                final int size = entity.world.getEntitiesWithinAABBExcludingEntity(entity, entity.getCollisionBoundingBox().grow(1, 1, 1))
                                        .size();
                                if (size < 5) {
                                    continue;
                                }
                                entityCollisionCounts.put(entity, size);
                            }
                        }
                    }

                    final Object2IntMap<ChunkPos> chunkTileCounts = new Object2IntOpenHashMap<>();
                    chunkTileCounts.defaultReturnValue(0);
                    final Reference2IntMap<Class> classTileCounts = new Reference2IntOpenHashMap<>();
                    classTileCounts.defaultReturnValue(0);
                    writer.name("tiles").beginArray();
                    for (int i = 0; i < world.loadedTileEntityList.size(); i++) {
                        final TileEntity tile = world.loadedTileEntityList.get(i);
                        if (logAll) {
                            writer.beginObject();
                            writer.name("type").value(tile.getClass().toString());
                            writer.name("x").value(tile.getPos().getX());
                            writer.name("y").value(tile.getPos().getY());
                            writer.name("z").value(tile.getPos().getZ());
                            writer.name("isInvalid").value(tile.isInvalid());
                            // writer.name("canUpdate").value(tile.canUpdate());
                            writer.name("block").value("" + tile.getBlockType());
                            writer.endObject();
                        }
                        final ChunkPos chunkCoords = new ChunkPos(tile.getPos().getX() >> 4, tile.getPos().getZ() >> 4);
                        chunkTileCounts.put(chunkCoords, chunkTileCounts.getInt(chunkCoords) + 1);
                        classTileCounts.put(tile.getClass(), classTileCounts.getInt(tile.getClass()) + 1);
                    }
                    writer.endArray();

                    if (logAll) {
                        writeChunkCounts(writer, "topEntityColliders", entityCollisionCounts, 20);
                    }

                    writeChunkCounts(writer, "entitiesByClass", classEntityCounts);
                    writeChunkCounts(writer, "entitiesByChunk", chunkEntityCounts);

                    writeChunkCounts(writer, "tilesByClass", classTileCounts);
                    writeChunkCounts(writer, "tilesByChunk", chunkTileCounts);

                    writer.endObject(); // Dimension
                }
                writer.endArray(); // Dimensions
            }
        } catch (Throwable throwable) {
            SpongeImpl.getLogger().error("Could not save chunk info report to " + file);
        }
    }

    private static <T> void writeChunkCounts(final JsonWriter writer, final String name, final Object2IntMap<T> map) throws IOException {
        writeChunkCounts(writer, name, map, 0);
    }

    private static <T> void writeChunkCounts(final JsonWriter writer, final String name, final Object2IntMap<T> map, final int max) throws IOException {
        final List<T> sortedCoords = new ArrayList<>(map.keySet());
        sortedCoords.sort((s1, s2) -> map.getInt(s2) - map.getInt(s1));

        int i = 0;
        writer.name(name).beginArray();
        for (final T key : sortedCoords) {
            if ((max > 0) && (i++ > max)) {
                break;
            }
            if (map.get(key) < 5) {
                continue;
            }
            writer.beginObject();
            writer.name("key").value(key.toString());
            writer.name("count").value(map.get(key));
            writer.endObject();
        }
        writer.endArray();
    }

    private static <T> void writeChunkCounts(final JsonWriter writer, final String name, final Reference2IntMap<T> map) throws IOException {
        writeChunkCounts(writer, name, map, 0);
    }

    private static <T> void writeChunkCounts(final JsonWriter writer, final String name, final Reference2IntMap<T> map, final int max) throws IOException {
        final List<T> sortedCoords = new ArrayList<>(map.keySet());
        sortedCoords.sort((s1, s2) -> map.getInt(s2) - map.getInt(s1));

        int i = 0;
        writer.name(name).beginArray();
        for (final T key : sortedCoords) {
            if ((max > 0) && (i++ > max)) {
                break;
            }
            if (map.get(key) < 5) {
                continue;
            }
            writer.beginObject();
            writer.name("key").value(key.toString());
            writer.name("count").value(map.get(key));
            writer.endObject();
        }
        writer.endArray();
    }


}
