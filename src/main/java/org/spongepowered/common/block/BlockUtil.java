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
package org.spongepowered.common.block;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.common.util.VecHelper;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class BlockUtil {

    public static final Comparator<BlockState> BLOCK_STATE_COMPARATOR = new BlockStateComparator();
    public static final UUID INVALID_WORLD_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public static boolean setBlockState(World world, int x, int y, int z, BlockState state, boolean notifyNeighbors) {
        return setBlockState(world, new BlockPos(x, y, z), state, notifyNeighbors);
    }

    public static boolean setBlockState(World world, BlockPos position, BlockState state, boolean notifyNeighbors) {
        return world.setBlockState(position, toNative(state), notifyNeighbors ? 3 : 2);
    }

    public static boolean setBlockState(Chunk chunk, int x, int y, int z, BlockState state, boolean notifyNeighbors) {
        return setBlockState(chunk, new BlockPos(x, y, z), state, notifyNeighbors);
    }

    public static boolean setBlockState(Chunk chunk, BlockPos position, BlockState state, boolean notifyNeighbors) {
        if (notifyNeighbors) { // delegate to world
            return setBlockState(chunk.getWorld(), position, state, true);
        }
        return chunk.setBlockState(position, toNative(state)) != null;
    }

    public static IBlockState toNative(BlockState state) {
        if (state instanceof IBlockState) {
            return (IBlockState) state;
        } else {
            // TODO: Need to figure out what is sensible for other BlockState
            // implementing classes.
            throw new UnsupportedOperationException("Custom BlockState implementations are not supported");
        }
    }

    public static BlockState fromNative(IBlockState blockState) {
        if (blockState instanceof BlockState) {
            return (BlockState) blockState;
        } else {
            // TODO: Need to figure out what is sensible for other BlockState
            // implementing classes.
            throw new UnsupportedOperationException("Custom BlockState implementations are not supported");
        }
    }

    public static BlockType toBlock(IBlockState state) {
        return fromNative(state).getType();
    }

    public static Block toBlock(BlockState state) {
        return toNative(state).getBlock();
    }

    public static IBlockState getBlockState(org.spongepowered.api.world.World world, Vector3i blockPos) {
        if (!(world instanceof World)) {
            throw new IllegalArgumentException("World : " + world.getName() + " is not appropriate for this implementation!");
        }
        return ((World) world).getBlockState(VecHelper.toBlockPos(blockPos));
    }

    private BlockUtil() {
    }

    private static final class BlockStateComparator implements Comparator<BlockState> {

        BlockStateComparator() {
        }

        @Override
        public int compare(BlockState spongeA, BlockState spongeB) {
            IBlockState a = (IBlockState) spongeA;
            IBlockState b = (IBlockState) spongeB;
            ComparisonChain chain = ComparisonChain.start();
            // compare IDs
            chain = chain.compare(a.getBlock().getUnlocalizedName(), b.getBlock().getUnlocalizedName());
            // compare block traits
            Map<BlockTrait<?>, ?> aTraits = spongeA.getTraitMap();
            Map<BlockTrait<?>, ?> bTraits = spongeA.getTraitMap();
            chain = chain.compare(aTraits.size(), bTraits.size());
            if (chain.result() != 0) {
                // avoid potentially expensive ops
                return chain.result();
            }
            MapDifference<BlockTrait<?>, ?> diff = Maps.difference(aTraits, bTraits);
            if (diff.areEqual()) {
                // When the Maps are equal the end-result is 0, so chain.result
                // is the same
                return chain.result();
            }
            // Check the keys, see if they match
            int onLeft = diff.entriesOnlyOnLeft().size();
            int onRight = diff.entriesOnlyOnRight().size();
            chain = chain.compare(onLeft, onRight);
            if (chain.result() != 0) {
                // avoid potentially expensive ops
                return chain.result();
            }
            // Ok, keys match. Check values. Guaranteed difference here due to
            // equality check above.
            List<BlockTrait<?>> checkOrder = sortTraits(diff.entriesDiffering().keySet());
            for (BlockTrait<?> trait : checkOrder) {
                Comparable<?> aTraitValue = (Comparable<?>) aTraits.get(trait);
                Comparable<?> bTraitValue = (Comparable<?>) bTraits.get(trait);
                chain = chain.compare(aTraitValue, bTraitValue);
                if (chain.result() != 0) {
                    return chain.result();
                }
            }
            // Impossible.
            throw new IllegalStateException("Some object's equals() violates contract!");
        }

        /**
         * Sorts {@code traits} by the trait IDs.
         */
        private List<BlockTrait<?>> sortTraits(Set<BlockTrait<?>> traits) {
            return traits.stream().sorted(Comparator.comparing(CatalogType::getId)).collect(Collectors.toList());
        }

    }
}
