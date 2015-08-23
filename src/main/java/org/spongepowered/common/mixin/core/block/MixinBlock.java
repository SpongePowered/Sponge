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

import org.spongepowered.api.event.source.world.WorldTickBlockEvent;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.item.ItemBlock;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.interfaces.block.IMixinBlock;
import org.spongepowered.common.text.translation.SpongeTranslation;
import org.spongepowered.common.util.VecHelper;

import java.util.Random;

@NonnullByDefault
@Mixin(value = Block.class, priority = 999)
public abstract class MixinBlock implements BlockType, IMixinBlock {

    @Shadow private boolean needsRandomTick;

    @Shadow public abstract boolean isBlockNormalCube();
    @Shadow public abstract boolean getEnableStats();
    @Shadow public abstract int getLightValue();
    @Shadow public abstract String getUnlocalizedName();
    @Shadow public abstract IBlockState getStateFromMeta(int meta);
    @Shadow public abstract Material getMaterial();
    @Shadow(prefix = "shadow$")
    public abstract IBlockState shadow$getDefaultState();

    @Override
    public String getId() {
        return Block.blockRegistry.getNameForObject(this).toString();
    }

    @Override
    public String getName() {
        return Block.blockRegistry.getNameForObject(this).toString();
    }

    @Override
    public BlockState getDefaultState() {
        return (BlockState) shadow$getDefaultState();
    }

    @Override
    public Translation getTranslation() {
        return new SpongeTranslation(getUnlocalizedName() + ".name");
    }

    @Override
    public boolean isLiquid() {
        return BlockLiquid.class.isAssignableFrom(this.getClass());
    }

    @Override
    public boolean isSolidCube() {
        return isBlockNormalCube();
    }

    @Override
    public boolean isAffectedByGravity() {
        return BlockFalling.class.isAssignableFrom(this.getClass());
    }

    @Override
    public boolean areStatisticsEnabled() {
        return getEnableStats();
    }

    @Override
    public float getEmittedLight() {
        return 15F / getLightValue();
    }

    @Override
    @Overwrite
    public boolean getTickRandomly() {
        return this.needsRandomTick;
    }

    @Override
    public void setTickRandomly(boolean tickRandomly) {
        this.needsRandomTick = tickRandomly;
    }

    @Inject(method = "randomTick", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
    public void callRandomTickEvent(World world, BlockPos pos, IBlockState state, Random rand, CallbackInfo ci) {
        final WorldTickBlockEvent event = SpongeEventFactory.createWorldTickBlock(Sponge.getGame(), null,
            new Location<org.spongepowered.api.world.World>((org.spongepowered.api.world.World)world, VecHelper.toVector(pos))); //TODO Fix null Cause
        Sponge.getGame().getEventManager().post(event);
        if(event.isCancelled()) {
            ci.cancel();
        }
    }

    @Override
    public boolean isReplaceable() {
        return getMaterial().isReplaceable();
    }

    @Override
    public Optional<ItemBlock> getHeldItem() {
        return Optional.fromNullable((ItemBlock) Item.getItemFromBlock((Block) (Object) this));
    }

    @Override
    public boolean isGaseous() {
        return this.getMaterial() == Material.air;
    }

    @Override
    public ImmutableList<ImmutableDataManipulator<?, ?>> getManipulators(World world, BlockPos blockPos) {
        return ImmutableList.of();
    }

    @Override
    public ImmutableList<ImmutableDataManipulator<?, ?>> getManipulators(IBlockState blockState) {
        return ImmutableList.of();
    }

    @Override
    public void resetBlockState(World world, BlockPos blockPos) {
        world.setBlockState(blockPos, shadow$getDefaultState());
    }

    @Override
    public BlockState getDefaultBlockState() {
        return getDefaultState();
    }
}
