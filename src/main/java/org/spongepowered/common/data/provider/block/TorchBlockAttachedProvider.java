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

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.block.RedstoneWallTorchBlock;
import net.minecraft.block.TorchBlock;
import net.minecraft.block.WallTorchBlock;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.BlockStateDataProvider;

import java.util.Optional;

public class TorchBlockAttachedProvider extends BlockStateDataProvider<Boolean> {

    TorchBlockAttachedProvider() {
        super(Keys.ATTACHED.get(), TorchBlock.class);
    }

    @Override
    protected Optional<Boolean> getFrom(BlockState dataHolder) {
        return Optional.of(dataHolder.getBlock() instanceof WallTorchBlock ||
                dataHolder.getBlock() instanceof RedstoneWallTorchBlock);
    }

    @Override
    protected Optional<BlockState> set(BlockState dataHolder, Boolean value) {
        final Block block = dataHolder.getBlock();
        final boolean isWallBlock = block instanceof WallTorchBlock || block instanceof RedstoneWallTorchBlock;
        if (value == isWallBlock) {
            return Optional.of(dataHolder);
        }
        if (block instanceof RedstoneTorchBlock) {
            return Optional.of((isWallBlock ? Blocks.REDSTONE_WALL_TORCH : Blocks.REDSTONE_TORCH).getDefaultState()
                    .with(RedstoneTorchBlock.LIT, dataHolder.get(RedstoneTorchBlock.LIT)));
        }
        return Optional.of((isWallBlock ? Blocks.WALL_TORCH : Blocks.TORCH).getDefaultState());
    }
}

