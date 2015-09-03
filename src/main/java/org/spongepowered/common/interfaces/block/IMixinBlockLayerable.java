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

import net.minecraft.block.BlockCake;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.manipulator.mutable.block.LayeredData;

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

    BlockState setLayerData(IBlockState blockState, LayeredData data);

}
