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
package org.spongepowered.common.data.provider.block;

import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.WallSkullBlock;
import net.minecraft.util.registry.Registry;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.BlockStateDataProvider;
import org.spongepowered.common.data.util.StateHelper;
import org.spongepowered.common.mixin.accessor.block.AbstractSkullBlockAccessor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AbstractSkullBlockAttachedProvider extends BlockStateDataProvider<Boolean> {

    static final class Pair {

        final SkullBlock groundBlock;
        final WallSkullBlock wallBlock;

        private Pair(SkullBlock groundBlock, WallSkullBlock wallBlock) {
            this.groundBlock = groundBlock;
            this.wallBlock = wallBlock;
        }
    }

    private final Map<SkullBlock.ISkullType, @Nullable Pair> wallAndGroundPairs = new HashMap<>();

    AbstractSkullBlockAttachedProvider() {
        super(Keys.ATTACHED, AbstractSkullBlock.class);
    }

    @Override
    protected Optional<Boolean> getFrom(BlockState dataHolder) {
        return Optional.of(dataHolder.getBlock() instanceof WallSkullBlock);
    }

    @Override
    protected Optional<BlockState> set(BlockState dataHolder, Boolean value) {
        final AbstractSkullBlock block = (AbstractSkullBlock) dataHolder.getBlock();
        final boolean isWallBlock = block instanceof WallSkullBlock;
        if (value == isWallBlock) {
            return Optional.of(dataHolder);
        }
        final SkullBlock.ISkullType type = ((AbstractSkullBlockAccessor) block).accessor$getSkullType();
        // Find the ground/wall pair based on the skull type
        @Nullable final Pair pair = this.wallAndGroundPairs.computeIfAbsent(type, type1 -> {
            @Nullable final SkullBlock groundBlock = (SkullBlock) Registry.BLOCK.stream()
                    .filter(b -> b instanceof SkullBlock &&
                            ((AbstractSkullBlockAccessor) block).accessor$getSkullType() == type)
                    .findFirst().orElse(null);
            if (groundBlock == null) {
                return null;
            }
            @Nullable final WallSkullBlock wallBlock = (WallSkullBlock) Registry.BLOCK.stream()
                    .filter(b -> b instanceof WallSkullBlock &&
                            ((AbstractSkullBlockAccessor) block).accessor$getSkullType() == type)
                    .findFirst().orElse(null);
            if (wallBlock == null) {
                return null;
            }
            return new Pair(groundBlock, wallBlock);
        });
        if (pair == null) {
            return Optional.of(dataHolder);
        }
        final Block newType = value ? pair.wallBlock : pair.groundBlock;
        return Optional.of(StateHelper.copyStatesFrom(newType.getDefaultState(), dataHolder));
    }
}
