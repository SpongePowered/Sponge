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
package org.spongepowered.common.mixin.optimization.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.level.BlockGetterBridge;

@Mixin(value = BlockGetter.class, priority = 1500)
public interface BlockGetterMixin_RayTraceChunkLoadOptimizations {

    // @formatter:off
    @Shadow BlockState shadow$getBlockState(BlockPos pos);
    @Shadow FluidState shadow$getFluidState(BlockPos pos);
    @Shadow BlockHitResult shadow$clipWithInteractionOverride(Vec3 startVec, Vec3 endVec, BlockPos pos, VoxelShape shape, BlockState state);
    // @formatter:on

    /**
     * Can be modified later when
     * <a href="https://github.com/SpongePowered/Mixin/issues/355">the mixin feature</a>
     * is resolved.
     * <p>
     * Check if a chunk is loaded before attempting to check the state of the block
     * return null if the chunk is not loaded. Based on
     * <a href="https://github.com/PaperMC/Paper/blob/ver/1.15.2/Spigot-Server-Patches/0331-Prevent-rayTrace-from-loading-chunks.patch#L16">a Paper patch</a>
     *
     * @author gabizou - Minecraft 1.15.2 - October 16th, 2020
     * @reason Because this patch requires a lambda injection, I don't want
     * to risk the breakages that can be caused by injecting into an interface.
     */
    @Overwrite
    default BlockHitResult clip(final ClipContext context) {
        return BlockGetter.traverseBlocks(context, (p_217297_1_, p_217297_2_) -> {

            // Sponge start - check if the blockstate is loaded/null
            // final BlockState blockstate = this.shadow$getBlockState(p_217297_2_); // Vanilla
            final @Nullable BlockState lvt_3_1_ = ((BlockGetterBridge) this).bridge$getBlockIfLoaded(p_217297_2_);
            if (lvt_3_1_ == null) {
                // copied the last function parameter (listed below)
                final Vec3 vec3d = p_217297_1_.getFrom().subtract(p_217297_1_.getTo());
                return BlockHitResult.miss(
                    context.getTo(),
                    Direction.getNearest(vec3d.x, vec3d.y, vec3d.z),
                    new BlockPos(p_217297_1_.getTo())
                );
            }
            // Sponge end
            final FluidState lvt_4_1_ = this.shadow$getFluidState(p_217297_2_);
            final Vec3 lvt_5_1_ = p_217297_1_.getFrom();
            final Vec3 lvt_6_1_ = p_217297_1_.getTo();
            final VoxelShape lvt_7_1_ = p_217297_1_.getBlockShape(lvt_3_1_, (BlockGetter) this, p_217297_2_);
            final BlockHitResult lvt_8_1_ = this.shadow$clipWithInteractionOverride(lvt_5_1_, lvt_6_1_, p_217297_2_, lvt_7_1_, lvt_3_1_);
            final VoxelShape lvt_9_1_ = p_217297_1_.getFluidShape(lvt_4_1_, (BlockGetter) this, p_217297_2_);
            final BlockHitResult lvt_10_1_ = lvt_9_1_.clip(lvt_5_1_, lvt_6_1_, p_217297_2_);
            final double lvt_11_1_ = lvt_8_1_ == null ? 1.7976931348623157E308D : p_217297_1_.getFrom().distanceToSqr(lvt_8_1_.getLocation());
            final double lvt_13_1_ = lvt_10_1_ == null ? 1.7976931348623157E308D : p_217297_1_.getFrom().distanceToSqr(lvt_10_1_.getLocation());
            return lvt_11_1_ <= lvt_13_1_ ? lvt_8_1_ : lvt_10_1_;
        }, (p_217302_0_) -> {
            final Vec3 lvt_1_1_ = p_217302_0_.getFrom().subtract(p_217302_0_.getTo());
            return BlockHitResult.miss(
                    p_217302_0_.getTo(),
                    Direction.getNearest(lvt_1_1_.x, lvt_1_1_.y, lvt_1_1_.z),
                    new BlockPos(p_217302_0_.getTo()));
        });
    }

}
