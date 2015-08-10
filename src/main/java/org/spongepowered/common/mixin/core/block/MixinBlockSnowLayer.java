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
package org.spongepowered.common.mixin.core.block;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.common.data.DataTransactionBuilder.successNoData;
import static org.spongepowered.common.data.DataTransactionBuilder.successReplaceData;

import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.manipulator.block.LayeredData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.manipulator.block.SpongeLayeredData;
import org.spongepowered.common.interfaces.block.IMixinBlockLayerable;

@Mixin(BlockSnow.class)
public abstract class MixinBlockSnowLayer extends MixinBlock implements IMixinBlockLayerable {

    private static final int LAYER_OFFSET = 1;

    @Override
    public LayeredData getLayerData(IBlockState blockState) {
        final int layer = (Integer) blockState.getValue(BlockSnow.LAYERS);
        return new SpongeLayeredData(7).setValue(layer - LAYER_OFFSET);
    }

    @Override
    public DataTransactionResult setLayerData(LayeredData layeredData, World world, BlockPos blockPos, DataPriority priority) {
        final LayeredData data = getLayerData(checkNotNull(world).getBlockState(checkNotNull(blockPos)));
        switch (checkNotNull(priority)) {
            case DATA_MANIPULATOR:
            case POST_MERGE:
                final IBlockState blockState = world.getBlockState(blockPos).withProperty(BlockSnow.LAYERS,
                        checkNotNull(layeredData).getValue() + LAYER_OFFSET);
                world.setBlockState(blockPos, blockState);
                return successReplaceData(data);
            default:
                return successNoData();
        }
    }

    @Override
    public BlockState resetLayerData(BlockState blockState) {
        return ((BlockState) ((IBlockState) blockState).withProperty(BlockSnow.LAYERS, 1));
    }

}
