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
package org.spongepowered.common.mixin.api.mcp.block;

import net.minecraft.block.Block;
import net.minecraft.fluid.IFluidState;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.fluid.FluidState;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.mixin.api.mcp.state.StateHolderMixin_API;
import org.spongepowered.common.util.Constants;

@Mixin(net.minecraft.block.BlockState.class)
public abstract class BlockStateMixin_API extends StateHolderMixin_API<BlockState, net.minecraft.block.BlockState> implements BlockState {

    @Shadow public abstract Block shadow$getBlock();
    @Shadow public abstract IFluidState shadow$getFluidState();

    @Override
    public BlockType getType() {
        return (BlockType) this.shadow$getBlock();
    }

    @Override
    public FluidState getFluidState() {
        return (FluidState) this.shadow$getFluidState();
    }

    @Override
    public int getContentVersion() {
        return Constants.Sponge.BlockState.STATE_AS_CATALOG_ID;
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew()
            .set(Queries.CONTENT_VERSION, this.getContentVersion())
            .set(Constants.Block.BLOCK_STATE, this.getId());
    }
}
