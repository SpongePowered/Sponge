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
package org.spongepowered.common.interfaces.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCake;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.manipulator.block.LayeredData;

/**
 * This interface provides the necessary information for {@link LayeredData} to be
 * retrieved from the various blocks that have "layered" information, such as
 * {@link BlockSnow}, {@link BlockCake}.
 */
public interface IMixinBlockLayerable extends IMixinBlock {

    /**
     * Gets the {@link LayeredData} from the given block state that
     * is guaranteed to be of the desired "type".
     *
     * @param blockState The block state to retrieve the layered data from
     * @return The layered data
     */
    LayeredData getLayerData(IBlockState blockState);

    /**
     * Sets the given {@link LayeredData} onto the block at the given
     * {@link BlockPos} of the given {@link World}. Since the
     * mixed in {@link Block} will have the appropriate {@link IProperty},
     * the transaction should almost always result in success, provided
     * that the {@link LayeredData} has the correct bounds.
     *
     * @param layeredData The layered data
     * @param world The world
     * @param blockPos The block position to set the layered data onto
     * @param priority The priority of data merging
     * @return The transaction result
     */
    DataTransactionResult setLayerData(LayeredData layeredData, World world, BlockPos blockPos, DataPriority priority);

    /**
     * Resets the direction on the given {@link BlockState}
     * @param blockState
     * @return
     */
    BlockState resetLayerData(BlockState blockState);

}
