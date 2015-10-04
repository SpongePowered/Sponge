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

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.TickBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;
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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
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
    @Overwrite
    public boolean getTickRandomly() {
        return this.needsRandomTick;
    }

    @Override
    public void setTickRandomly(boolean tickRandomly) {
        this.needsRandomTick = tickRandomly;
    }

    @Inject(method = "randomTick", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
    public void callRandomTickEvent(net.minecraft.world.World world, BlockPos pos, IBlockState state, Random rand, CallbackInfo ci) {
        BlockSnapshot blockSnapshot = ((World) world).createSnapshot(VecHelper.toVector(pos));
        final TickBlockEvent event = SpongeEventFactory.createTickBlockEvent(Sponge.getGame(), Cause.of(world), blockSnapshot);
        Sponge.getGame().getEventManager().post(event);
        if(event.isCancelled()) {
            ci.cancel();
        }
    }

    @Override
    public boolean supports(Class<? extends ImmutableDataManipulator<?, ?>> immutable) {
        return false;
    }

    @Override
    public Optional<BlockState> getStateWithData(IBlockState blockState, ImmutableDataManipulator<?, ?> manipulator) {
        return Optional.empty();
    }

    @Override
    public <E> Optional<BlockState> getStateWithValue(IBlockState blockState, Key<? extends BaseValue<E>> key, E value) {
        return Optional.empty(); // By default, all blocks just have a single state unless otherwise dictated.
    }

    @Override
    public List<ImmutableDataManipulator<?, ?>> getManipulators(IBlockState blockState) {
        return ImmutableList.of();
    }

    @Override
    public BlockState getDefaultBlockState() {
        return getDefaultState();
    }

    @Override
    public Collection<BlockTrait<?>> getTraits() {
        return getDefaultBlockState().getTraits();
    }

    @Override
    public Optional<BlockTrait<?>> getTrait(String blockTrait) {
        return getDefaultBlockState().getTrait(blockTrait);
    }
}
