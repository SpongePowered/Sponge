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
package org.spongepowered.common.mixin.api.minecraft.world.level.block.state;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.util.mirror.Mirror;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import java.util.Objects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FluidState;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockBehaviour_BlockStateBaseMixin_API extends StateHolderMixin_API<BlockState, net.minecraft.world.level.block.state.BlockState> implements BlockState {

    // @formatter:off
    @Shadow public abstract Block shadow$getBlock();
    @Shadow public abstract FluidState shadow$getFluidState();
    @Shadow public abstract net.minecraft.world.level.block.state.BlockState shadow$rotate(net.minecraft.world.level.block.Rotation rotation);
    @Shadow public abstract net.minecraft.world.level.block.state.BlockState shadow$mirror(net.minecraft.world.level.block.Mirror rotation);
    // @formatter:on

    @Override
    public BlockType type() {
        return (BlockType) this.shadow$getBlock();
    }

    @Override
    public org.spongepowered.api.fluid.FluidState fluidState() {
        return (org.spongepowered.api.fluid.FluidState) (Object) this.shadow$getFluidState();
    }

    @Override
    public BlockState rotate(final Rotation rotation) {
        return (BlockState) this.shadow$rotate((net.minecraft.world.level.block.Rotation) (Object) Objects.requireNonNull(rotation, "Rotation cannot be null!"));
    }

    @Override
    public BlockState mirror(final Mirror mirror) {
        return (BlockState) this.shadow$mirror((net.minecraft.world.level.block.Mirror) (Object) Objects.requireNonNull(mirror, "Mirror cannot be null!"));
    }

}
