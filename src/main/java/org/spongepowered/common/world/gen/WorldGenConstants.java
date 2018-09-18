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
package org.spongepowered.common.world.gen;

import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.ChunkGeneratorEnd;
import net.minecraft.world.gen.ChunkGeneratorFlat;
import net.minecraft.world.gen.ChunkGeneratorHell;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.util.weighted.SeededVariableAmount;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Random;
import java.util.function.Predicate;

public final class WorldGenConstants {

    public static final String VILLAGE_FLAG = "VILLAGE";

    private static final Class<?>[] MIXINED_CHUNK_PROVIDERS =
            new Class<?>[] {ChunkGeneratorOverworld.class, ChunkGeneratorFlat.class, ChunkGeneratorHell.class, ChunkGeneratorEnd.class};

    public static boolean isValid(IChunkGenerator cp, Class<?> api_type) {
        if (api_type.isInstance(cp)) {
            for (Class<?> mixind : MIXINED_CHUNK_PROVIDERS) {
                if (cp.getClass().equals(mixind)) {
                    return true;
                }
                // If our chunk provider is an instance of one of our mixed in classes but is not the class
                // then its a custom chunk provider which is extending one of the vanilla classes but if we
                // use it as a generation populator directly then we would lose the custom logic of the
                // extending class so we wrap it instead so that the provideChunk method is called.
                if(mixind.isInstance(cp)) {
                    // This checks that if the custom generator in fact does directly implement our interface,
                    // with the assurance (according to Reflection API) that the target class is actually
                    // extending the api type interface. This allows for mods that use our api and extend
                    // a mixed in type target to still implement our interface.
                    for (Class<?> anInterface : cp.getClass().getInterfaces()) {
                        if (api_type.equals(anInterface)) {
                            return true;
                        }
                    }
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static final SeededVariableAmount<Double> GROUND_COVER_DEPTH = new SeededVariableAmount<Double>() {

        @Override
        public double getAmount(Random rand, Double seed) {
            return (int) (seed / 3.0D + 3.0D + rand.nextDouble() * 0.25D);
        }

    };

    public static final Predicate<BlockState> DIRT_OR_GRASS = (input) -> {
        return input.getType().equals(BlockTypes.DIRT) || input.getType().equals(BlockTypes.GRASS);
    };

    public static final Predicate<BlockState> DIRT = (input) -> {
        return input.getType().equals(BlockTypes.DIRT);
    };

    public static final Predicate<BlockState> STONE = (input) -> {
        return input.getType().equals(BlockTypes.STONE);
    };

    public static final Predicate<Location<World>> STONE_LOCATION = (input) -> {
        return input.getBlock().getType().equals(BlockTypes.STONE);
    };

    public static final Predicate<Location<World>> CAVE_LIQUIDS = (input) -> {
        if (input.getBlockY() <= 0 || input.getBlockY() >= 255) {
            return false;
        }
        if (input.add(0, 1, 0).getBlock().getType() != BlockTypes.STONE || input.add(0, -1, 0).getBlock().getType() != BlockTypes.STONE
                || (input.getBlock().getType() != BlockTypes.STONE && input.getBlock().getType() != BlockTypes.AIR)) {
            return false;
        }
        int air = 0;
        int stone = 0;
        if (input.add(1, 0, 0).getBlock().getType() == BlockTypes.STONE) {
            stone++;
        }
        if (input.add(-1, 0, 0).getBlock().getType() == BlockTypes.STONE) {
            stone++;
        }
        if (input.add(0, 0, 1).getBlock().getType() == BlockTypes.STONE) {
            stone++;
        }
        if (input.add(0, 0, -1).getBlock().getType() == BlockTypes.STONE) {
            stone++;
        }
        if (input.add(1, 0, 0).getBlock().getType() == BlockTypes.AIR) {
            air++;
        }
        if (input.add(-1, 0, 0).getBlock().getType() == BlockTypes.AIR) {
            air++;
        }
        if (input.add(0, 0, 1).getBlock().getType() == BlockTypes.AIR) {
            air++;
        }
        if (input.add(0, 0, -1).getBlock().getType() == BlockTypes.AIR) {
            air++;
        }
        if (air == 1 && stone == 3) {
            return true;
        }
        return false;
    };

    public static final Predicate<Location<World>> HELL_LAVA = (input) -> {
        if (input.add(0, 1, 0).getBlockType() != BlockTypes.NETHERRACK) {
            return false;
        } else if (input.getBlockType() != BlockTypes.AIR && input.getBlockType() != BlockTypes.NETHERRACK) {
            return false;
        }
        int i = 0;

        if (input.add(-1, 0, 0).getBlockType() == BlockTypes.NETHERRACK) {
            ++i;
        }

        if (input.add(1, 0, 0).getBlockType() == BlockTypes.NETHERRACK) {
            ++i;
        }

        if (input.add(0, 0, -1).getBlockType() == BlockTypes.NETHERRACK) {
            ++i;
        }

        if (input.add(0, 0, 1).getBlockType() == BlockTypes.NETHERRACK) {
            ++i;
        }

        if (input.add(0, -1, 0).getBlockType() == BlockTypes.NETHERRACK) {
            ++i;
        }

        int j = 0;

        if (input.add(-1, 0, 0).getBlockType() == BlockTypes.AIR) {
            ++j;
        }

        if (input.add(1, 0, 0).getBlockType() == BlockTypes.AIR) {
            ++j;
        }

        if (input.add(0, 0, -1).getBlockType() == BlockTypes.AIR) {
            ++j;
        }

        if (input.add(0, 0, 1).getBlockType() == BlockTypes.AIR) {
            ++j;
        }

        if (input.add(0, -1, 0).getBlockType() == BlockTypes.AIR) {
            ++j;
        }

        if (i == 4 && j == 1) {
            return true;
        }

        return false;
    };

    public static final Predicate<Location<World>> HELL_LAVA_ENCLOSED = (input) -> {
        if (input.add(0, 1, 0).getBlockType() != BlockTypes.NETHERRACK) {
            return false;
        } else if (input.getBlockType() != BlockTypes.AIR && input.getBlockType() != BlockTypes.NETHERRACK) {
            return false;
        }
        int i = 0;

        if (input.add(-1, 0, 0).getBlockType() == BlockTypes.NETHERRACK) {
            ++i;
        }

        if (input.add(1, 0, 0).getBlockType() == BlockTypes.NETHERRACK) {
            ++i;
        }

        if (input.add(0, 0, -1).getBlockType() == BlockTypes.NETHERRACK) {
            ++i;
        }

        if (input.add(0, 0, 1).getBlockType() == BlockTypes.NETHERRACK) {
            ++i;
        }

        if (input.add(0, -1, 0).getBlockType() == BlockTypes.NETHERRACK) {
            ++i;
        }

        if (i == 5) {
            return true;
        }
        return false;
    };

    public static boolean lightingEnabled = true;

    public static void disableLighting() {
        lightingEnabled = false;
    }

    public static void enableLighting() {
        lightingEnabled = true;
    }

    private WorldGenConstants() {

    }
}
