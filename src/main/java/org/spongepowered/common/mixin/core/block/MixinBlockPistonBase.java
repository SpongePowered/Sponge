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
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableDirectionalData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableExtendedData;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeDirectionalData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeExtendedData;
import org.spongepowered.common.data.util.DirectionResolver;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.block.BlockPhase;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.util.VecHelper;

import java.util.Optional;

@Mixin(BlockPistonBase.class)
public abstract class MixinBlockPistonBase extends MixinBlock {

    @Override
    public ImmutableList<ImmutableDataManipulator<?, ?>> getManipulators(IBlockState blockState) {
        return ImmutableList.<ImmutableDataManipulator<?, ?>>of(getIsExtendedFor(blockState), getDirectionalData(blockState));
    }

    @Override
    public boolean supports(Class<? extends ImmutableDataManipulator<?, ?>> immutable) {
        return ImmutableExtendedData.class.isAssignableFrom(immutable) || ImmutableDirectionalData.class.isAssignableFrom(immutable);
    }

    @Override
    public Optional<BlockState> getStateWithData(IBlockState blockState, ImmutableDataManipulator<?, ?> manipulator) {
        if (manipulator instanceof ImmutableExtendedData) {
            return Optional.of((BlockState) blockState);
        }
        if (manipulator instanceof ImmutableDirectionalData) {
            final Direction dir = ((ImmutableDirectionalData) manipulator).direction().get();
            return Optional.of((BlockState) blockState.withProperty(BlockPistonBase.FACING, DirectionResolver.getFor(dir)));
        }
        return super.getStateWithData(blockState, manipulator);
    }

    @Override
    public <E> Optional<BlockState> getStateWithValue(IBlockState blockState, Key<? extends BaseValue<E>> key, E value) {
        if (key.equals(Keys.EXTENDED)) {
            return Optional.of((BlockState) blockState);
        }
        if (key.equals(Keys.DIRECTION)) {
            final Direction dir = (Direction) value;
            return Optional.of((BlockState) blockState.withProperty(BlockPistonBase.FACING, DirectionResolver.getFor(dir)));
        }
        return super.getStateWithValue(blockState, key, value);
    }

    private ImmutableExtendedData getIsExtendedFor(IBlockState blockState) {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeExtendedData.class, blockState.getValue(BlockPistonBase.EXTENDED));
    }

    private ImmutableDirectionalData getDirectionalData(IBlockState blockState) {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeDirectionalData.class,
                DirectionResolver.getFor(blockState.getValue(BlockPistonBase.FACING)));
    }

    @Inject(method = "doMove", at = @At("HEAD"))
    public void onDoMoveHead(World worldIn, BlockPos pos, EnumFacing direction, boolean extending, CallbackInfoReturnable<Boolean> cir) {
        if (!worldIn.isRemote && CauseTracker.ENABLED) {
            final IMixinWorldServer mixinWorldServer = (IMixinWorldServer) worldIn;
            final SpongeBlockSnapshot spongeBlockSnapshot = mixinWorldServer.createSpongeBlockSnapshot(worldIn.getBlockState(pos), worldIn
                    .getBlockState(pos), pos, 3);
            final PhaseContext phaseContext = PhaseContext.start()
                    .add(NamedCause.source(spongeBlockSnapshot))
                    .addBlockCaptures()
                    .addEntityCaptures()
                    .addEntityDropCaptures();
            final IMixinChunk mixinChunk = (IMixinChunk) worldIn.getChunkFromBlockCoords(pos);
            mixinChunk.getBlockOwner(pos)
                    .ifPresent(phaseContext::owner);
            mixinChunk.getBlockNotifier(pos)
                    .ifPresent(phaseContext::notifier);
            phaseContext.add(NamedCause.source(LocatableBlock.builder().world((org.spongepowered.api.world.World) worldIn).position(VecHelper
                    .toVector3i(pos)).state((BlockState) worldIn.getBlockState(pos)).build()));

            mixinWorldServer.getCauseTracker().switchToPhase(BlockPhase.State.PISTON_MOVING, phaseContext
                    .complete());
        }
    }

    @Inject(method = "doMove", at = @At("RETURN"))
    public void onDoMoveEnd(World worldIn, BlockPos pos, EnumFacing direction, boolean extending, CallbackInfoReturnable<Boolean> cir) {
        if (!worldIn.isRemote && CauseTracker.ENABLED) {
            ((IMixinWorldServer) worldIn).getCauseTracker().completePhase();
        }
    }
}
