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

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.TeleportHelper;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class SpongeTeleportHelper implements TeleportHelper {

    @Override
    public Optional<Location<World>> getSafeLocation(Location<World> location) {
        return getSafeLocation(location, DEFAULT_HEIGHT, DEFAULT_WIDTH);
    }

    @Override
    public Optional<Location<World>> getSafeLocation(Location<World> location, final int height, final int width) {
        // Check around the player first in a configurable radius:
        final Optional<Location<World>> safe = checkAboveAndBelowLocation(location, height, width);
        if (safe.isPresent()) {
            // Add 0.5 to X and Z of block position so always in centre of block
            return Optional.of(new Location<>(safe.get().getExtent(), safe.get().getBlockPosition().toDouble().add(0.5, 0, 0.5)));
        }
        return safe;
    }

    private Optional<Location<World>> checkAboveAndBelowLocation(Location<World> location, final int height, final int width) {
        // For now this will just do a straight up block.
        // Check the main level
        Optional<Location<World>> safe = checkAroundLocation(location, width);

        if (safe.isPresent()) {
            return safe;
        }

        // We've already checked zero right above this.
        for (int currentLevel = 1; currentLevel <= height; currentLevel++) {
            // Check above
            safe = checkAroundLocation(location.add(0, currentLevel, 0), width);
            if (safe.isPresent()) {
                return safe;
            }

            // Check below
            safe = checkAroundLocation(location.add(0, -currentLevel, 0), width);
            if (safe.isPresent()) {
                return safe;
            }
        }

        return Optional.empty();
    }

    private Optional<Location<World>> checkAroundLocation(Location<World> location, final int radius) {
        if (isSafeLocation(location.getExtent(), location.getBlockPosition())) {
            return Optional.of(location);
        }

        // Now we're going to search in expanding concentric circles...
        for (int currentRadius = 0; currentRadius <= radius; currentRadius++) {
            Optional<Vector3i> safePosition = checkAroundSpecificDiameter(location, currentRadius);
            if (safePosition.isPresent()) {
                // If a safe area was found: Return the checkLoc, it is the safe
                // location.
                return Optional.of(new Location<>(location.getExtent(), safePosition.get()));
            }
        }

        return Optional.empty();
    }

    private Optional<Vector3i> checkAroundSpecificDiameter(Location<World> checkLoc, final int radius) {
        World world = checkLoc.getExtent();
        Vector3i blockPos = checkLoc.getBlockPosition();
        //example at radius 2
        //..c..
        //.....
        //..o..
        //.....
        //.....
        // Check out at the radius provided.
        blockPos = blockPos.add(radius, 0, 0);
        if (isSafeLocation(world, blockPos)) {
            return Optional.of(blockPos);
        }
        //example at radius 2
        //..c01
        //.....
        //..o..
        //.....
        //.....
        // Move up to the first corner..
        for (int i = 0; i < radius; i++) {
            blockPos = blockPos.add(0, 0, i);
            if (isSafeLocation(world, blockPos)) {
                return Optional.of(blockPos);
            }
        }
        //example at radius 2
        //....c
        //....0
        //..o.1
        //....2
        //....3
        // Move to the second corner..
        for (int i = 0; i < radius * 2; i++) {
            blockPos = blockPos.add(-i, 0, 0);
            if (isSafeLocation(world, blockPos)) {
                return Optional.of(blockPos);
            }
        }
        //example at radius 2
        //.....
        //.....
        //..o..
        //.....
        //3210c
        // Move to the third corner..
        for (int i = 0; i < radius * 2; i++) {
            blockPos = blockPos.add(0, 0, -i);
            if (isSafeLocation(world, blockPos)) {
                return Optional.of(blockPos);
            }
        }
        //example at radius 2
        //3....
        //2....
        //1.o..
        //0....
        //c....
        // Move to the last corner..
        for (int i = 0; i < radius * 2; i++) {
            blockPos = blockPos.add(i, 0, 0);
            if (isSafeLocation(world, blockPos)) {
                return Optional.of(blockPos);
            }
        }
        //example at radius 2
        //c0...
        //.....
        //..o..
        //.....
        //.....
        // Move back to just before the starting point.
        for (int i = 0; i < radius - 1; i++) {
            blockPos = blockPos.add(0, 0, i);
            if (isSafeLocation(world, blockPos)) {
                return Optional.of(blockPos);
            }
        }
        return Optional.empty();
    }

    public boolean isSafeLocation(World world, Vector3i blockPos) {
        final Vector3i up = blockPos.add(Vector3i.UP);
        final Vector3i down = blockPos.sub(Vector3i.UP);

        return !(!isBlockSafe(world, blockPos, false) || !isBlockSafe(world, up, false) || !isBlockSafe(world, down, true));

    }

    private boolean isBlockSafe(World world, Vector3i blockPos, boolean floorBlock) {
        if (blockPos.getY() <= 0) {
            return false;
        }

        if (blockPos.getY() > world.getDimension().getHeight()) {
            return false;
        }

        final BlockType block = world.getBlockType(blockPos);

        if (floorBlock) {
            // Floor is air so we'll fall, need to make sure we fall safely.
            if (block == BlockTypes.AIR) {
                final BlockType typeBelowPos = world.getBlockType(blockPos.sub(0, 1, 0));
                final BlockType typeBelowPos2 = world.getBlockType(blockPos.sub(0, 2, 0));

                // We'll fall too far, not safe
                if (typeBelowPos == BlockTypes.AIR && typeBelowPos2 == BlockTypes.AIR) {
                    return false;
                }

                // We'll fall onto a block, need to make sure its safe
                if (typeBelowPos != BlockTypes.AIR && !isSafeFloorMaterial(((Block) typeBelowPos).getMaterial((IBlockState) typeBelowPos.getDefaultState()))) {
                    return false;
                }

                // We'll fall through an air block to another, need to make sure
                // its safe
                return isSafeFloorMaterial(((Block) typeBelowPos2).getMaterial((IBlockState) typeBelowPos2.getDefaultState()));
            }

            // We have a non-air floor, need to ensure its safe
            return isSafeFloorMaterial(((Block) block).getMaterial((IBlockState) block.getDefaultState()));
        }

        // We need to make sure the block at our torso or head is safe
        return isSafeBodyMaterial(((Block) block).getMaterial((IBlockState) block.getDefaultState()));
    }

    private boolean isSafeFloorMaterial(Material material) {
        return !(material == Material.CACTUS || material == Material.FIRE || material == Material.LAVA);
    }

    private boolean isSafeBodyMaterial(Material material) {
        return (material == Material.AIR || material == Material.GRASS || material == Material.PLANTS
                || material == Material.WATER || material == Material.REDSTONE_LIGHT || material == Material.CIRCUITS
                || material == Material.SNOW || material == Material.PORTAL || material == Material.WEB || material == Material.VINE);
    }
}
