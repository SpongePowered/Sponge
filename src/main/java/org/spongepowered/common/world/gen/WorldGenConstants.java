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

import com.google.common.base.Predicate;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.util.weighted.SeededVariableAmount;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Random;

public final class WorldGenConstants {

    public static final String VILLAGE_FLAG = "VILLAGE";

    public static final SeededVariableAmount<Double> GROUND_COVER_DEPTH = new SeededVariableAmount<Double>() {

        @Override
        public double getAmount(Random rand, Double seed) {
            return (int) (seed / 3.0D + 3.0D + rand.nextDouble() * 0.25D);
        }

    };

    public static final Predicate<BlockState> DIRT_OR_GRASS = new Predicate<BlockState>() {

        @Override
        public boolean apply(BlockState input) {
            return input.getType().equals(BlockTypes.DIRT) || input.getType().equals(BlockTypes.GRASS);
        }

    };

    public static final Predicate<BlockState> DIRT = new Predicate<BlockState>() {

        @Override
        public boolean apply(BlockState input) {
            return input.getType().equals(BlockTypes.DIRT);
        }

    };

    public static final Predicate<BlockState> STONE = new Predicate<BlockState>() {

        @Override
        public boolean apply(BlockState input) {
            return input.getType().equals(BlockTypes.STONE);
        }

    };

    public static final Predicate<Location<World>> CAVE_LIQUIDS = new Predicate<Location<World>>() {

        @Override
        public boolean apply(Location<World> input) {
            if (input.getBlockY() <= 0 || input.getBlockY() >= 255) {
                return false;
            }
            if (input.add(0, 1, 0).getBlock().getType() != BlockTypes.STONE
                    || input.add(0, -1, 0).getBlock().getType() != BlockTypes.STONE
                    || (input.getBlock().getType() != BlockTypes.STONE && input
                            .getBlock().getType() != BlockTypes.AIR)) {
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
        }

    };

    public static final Predicate<Location<World>> HELL_LAVA = new Predicate<Location<World>>() {

        @Override
        public boolean apply(Location<World> input) {
            if (input.add(0, 1, 0).getBlockType() != BlockTypes.NETHERRACK) {
                return false;
            } else if (input.getBlockType() != BlockTypes.AIR
                    && input.getBlockType() != BlockTypes.NETHERRACK) {
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
        }
    };

    public static final Predicate<Location<World>> HELL_LAVA_ENCLOSED = new Predicate<Location<World>>() {

        @Override
        public boolean apply(Location<World> input) {
            if (input.add(0, 1, 0).getBlockType() != BlockTypes.NETHERRACK) {
                return false;
            } else if (input.getBlockType() != BlockTypes.AIR
                    && input.getBlockType() != BlockTypes.NETHERRACK) {
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
        }
    };

    private WorldGenConstants() {

    }
}
