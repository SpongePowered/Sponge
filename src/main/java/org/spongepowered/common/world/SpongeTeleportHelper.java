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
package org.spongepowered.common.world;

import com.flowpowered.math.GenericMath;
import com.flowpowered.math.vector.Vector3i;
import com.google.inject.Singleton;
import net.minecraft.block.material.Material;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.TeleportHelper;
import org.spongepowered.api.world.World;
import org.spongepowered.common.block.BlockUtil;
import org.spongepowered.common.interfaces.world.gen.IMixinChunkProviderServer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nullable;

@Singleton
public class SpongeTeleportHelper implements TeleportHelper {

    @Override
    public Optional<Location<World>> getSafeLocation(Location<World> location) {
        return getSafeLocation(location, DEFAULT_HEIGHT, DEFAULT_WIDTH);
    }

    @Override
    public Optional<Location<World>> getSafeLocation(Location<World> location, int height, int width) {
        final World world = location.getExtent();

        IMixinChunkProviderServer chunkProviderServer = (IMixinChunkProviderServer)((net.minecraft.world.WorldServer) world).getChunkProvider();
        chunkProviderServer.setForceChunkRequests(true);
        // We cache the various block lookup results so we don't check a block twice.
        final Map<Vector3i, BlockData> blockCache = new HashMap<>();

        // Get the vectors to check, and get the block types with them.
        // The vectors should be sorted by distance from the centre of the checking region, so
        // this makes it easier to try to get close, because we can just iterate and get progressively further out.
        Optional<Vector3i> result = getBlockLocations(location, height, width).filter(currentTarget -> {
            // Get the block, add it to the cache.
            BlockData block = getBlockData(currentTarget, world, blockCache);

            // If the block isn't safe, no point in continuing on this run.
            if (block.isSafeBody) {

                // Check the block ABOVE is safe for the body, and the two BELOW are safe too.
                if (getBlockData(currentTarget.add(0, 1, 0), world, blockCache).isSafeBody
                        && isFloorSafe(currentTarget, world, blockCache)) {

                    // This position should be safe. Get the center of the block to spawn into.
                    return true;
                }
            }

            return false;
        }).findFirst();

        chunkProviderServer.setForceChunkRequests(false);
        if (result.isPresent()) {
            return Optional.of(new Location<>(world, result.get().toDouble().add(0.5, 0, 0.5)));
        }

        // No vectors matched, so return an empty optional.
        return Optional.empty();
    }

    private boolean isFloorSafe(Vector3i currentTarget, World world, Map<Vector3i, BlockData> blockCache) {
        BlockData data = getBlockData(currentTarget.sub(0, 1, 0), world, blockCache);

        // If it's a safe floor, we can just say yes now.
        if (data.isSafeFloor) {
            return true;
        }

        // If it's not safe for the body, then we don't want to go through it anyway.
        if (!data.isSafeBody) {
            return false;
        }

        // Check the next block down, if it's a floor, then we're good to go, otherwise we'd fall too far for our liking.
        return getBlockData(currentTarget.sub(0, 2, 0), world, blockCache).isSafeFloor;
    }

    private Stream<Vector3i> getBlockLocations(Location<World> worldLocation, int height, int width) {
        // We don't want to warp outside of the world border, so we want to check that we're within it.
        WorldBorder worldBorder = (WorldBorder) worldLocation.getExtent().getWorldBorder();
        int worldBorderMinX = GenericMath.floor(worldBorder.minX());
        int worldBorderMinZ = GenericMath.floor(worldBorder.minZ());
        int worldBorderMaxX = GenericMath.floor(worldBorder.maxX());
        int worldBorderMaxZ = GenericMath.floor(worldBorder.maxZ());

        // Get the World and get the maximum Y value.
        int worldMaxY = worldLocation.getExtent().getBlockMax().getY();

        Vector3i vectorLocation = worldLocation.getBlockPosition();

        // We use clamp to remain within the world confines, so we don't waste time checking blocks outside of the
        // world border and the world height.
        int minY = GenericMath.clamp(vectorLocation.getY() - height, 0, worldMaxY);
        int maxY = GenericMath.clamp(vectorLocation.getY() + height, 0, worldMaxY);

        int minX = GenericMath.clamp(vectorLocation.getX() - width, worldBorderMinX, worldBorderMaxX);
        int maxX = GenericMath.clamp(vectorLocation.getX() + width, worldBorderMinX, worldBorderMaxX);

        int minZ = GenericMath.clamp(vectorLocation.getZ() - width, worldBorderMinZ, worldBorderMaxZ);
        int maxZ = GenericMath.clamp(vectorLocation.getZ() + width, worldBorderMinZ, worldBorderMaxZ);

        // We now iterate over all possible x, y and z positions to get all possible vectors.
        List<Vector3i> vectors = new ArrayList<>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    vectors.add(new Vector3i(x, y, z));
                }
            }
        }

        // Sort them according to the distance to the provided worldLocation.
        return vectors.stream().sorted(Comparator.comparingInt(vectorLocation::distanceSquared));
    }

    private BlockData getBlockData(Vector3i vector3i, World world, Map<Vector3i, BlockData> cache) {
        if (vector3i.getY() < 0) {
            // Anything below this isn't safe, no point going further.
            return new BlockData(null);
        }

        if (cache.containsKey(vector3i)) {
            return cache.get(vector3i);
        }

        BlockData data = new BlockData(BlockUtil.toNative(world.getBlock(vector3i)).getMaterial());
        cache.put(vector3i, data);
        return data;
    }

    private class BlockData {

        private final boolean isSafeBody;
        private final boolean isSafeFloor;

        // A null material just indicates that we want nothing to be marked as safe.
        private BlockData(@Nullable Material material) {
            this.isSafeBody = isSafeBodyMaterial(material);
            this.isSafeFloor = isSafeFloorMaterial(material);
        }

        private boolean isSafeFloorMaterial(@Nullable Material material) {
            return material != null && !(material == Material.AIR || material == Material.CACTUS || material == Material.FIRE
                    || material == Material.LAVA);
        }

        private boolean isSafeBodyMaterial(@Nullable Material material) {
            return material != null && (material == Material.AIR || material == Material.PLANTS
                    || material == Material.WATER || material == Material.REDSTONE_LIGHT || material == Material.CIRCUITS
                    || material == Material.SNOW || material == Material.PORTAL || material == Material.WEB || material == Material.VINE);
        }
    }
}
