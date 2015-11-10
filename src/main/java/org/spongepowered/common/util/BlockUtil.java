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
package org.spongepowered.common.util;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.trait.BlockTrait;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BlockUtil {

    private static final class BlockStateComparator implements Comparator<BlockState> {

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
            return traits.stream().sorted((a, b) -> a.getId().compareTo(b.getId())).collect(Collectors.toList());
        }

    }

    public static final Comparator<BlockState> BLOCK_STATE_COMPARATOR = new BlockStateComparator();

}
