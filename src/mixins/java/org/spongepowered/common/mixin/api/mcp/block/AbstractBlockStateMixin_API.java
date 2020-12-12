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

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.fluid.FluidState;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.util.mirror.Mirror;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.mixin.api.mcp.state.StateHolderMixin_API;

import java.util.Objects;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin_API extends StateHolderMixin_API<BlockState, net.minecraft.block.BlockState> implements BlockState {

    //@formatting:off
    @Shadow public abstract Block shadow$getBlock();
    @Shadow public abstract FluidState shadow$getFluidState();
    @Shadow public abstract net.minecraft.block.BlockState shadow$rotate(net.minecraft.util.Rotation rotation);
    @Shadow public abstract net.minecraft.block.BlockState shadow$mirror(net.minecraft.util.Mirror rotation);
    //@formatting:on

    @Override
    public BlockType getType() {
        return (BlockType) this.shadow$getBlock();
    }

    @Override
    public org.spongepowered.api.fluid.FluidState getFluidState() {
        return (org.spongepowered.api.fluid.FluidState) (Object) this.shadow$getFluidState();
    }

    @Override
    public BlockState rotate(final Rotation rotation) {
        return (BlockState) this.shadow$rotate((net.minecraft.util.Rotation) (Object) Objects.requireNonNull(rotation, "Rotation cannot be null!"));
    }

    @Override
    public BlockState mirror(final Mirror mirror) {
        return (BlockState) this.shadow$mirror((net.minecraft.util.Mirror) (Object) Objects.requireNonNull(mirror, "Mirror cannot be null!"));
    }

}
