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
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockNewLeaf;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableDecayableData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableTreeData;
import org.spongepowered.api.data.type.TreeType;
import org.spongepowered.api.data.type.TreeTypes;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeDecayableData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeTreeData;
import org.spongepowered.common.data.util.TreeTypeResolver;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.phase.block.BlockPhase;
import org.spongepowered.common.event.tracking.phase.TrackingPhases;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

import java.util.List;
import java.util.Optional;

@NonnullByDefault
@Mixin(BlockLeaves.class)
public abstract class MixinBlockLeaves extends MixinBlock {

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void onLeavesConstruction(CallbackInfo ci) {
        this.setTickRandomly(SpongeImpl.getGlobalConfig().getConfig().getWorld().getLeafDecay());
    }

    @Redirect(method = "updateTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z"))
    public boolean onUpdateDecayState(net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, int flags) {
        IMixinWorldServer spongeWorld = (IMixinWorldServer) worldIn;
        final CauseTracker causeTracker = spongeWorld.getCauseTracker();
        final boolean isBlockAlready = CauseTracker.ENABLED && causeTracker.getCurrentState().getPhase() != TrackingPhases.BLOCK;
        final IPhaseState currentState = causeTracker.getCurrentPhaseData().state;
        final boolean isWorldGen = currentState.getPhase().isWorldGeneration(currentState);
        final IBlockState actualState = state.getActualState(worldIn, pos);
        if (isBlockAlready && !isWorldGen) {
            causeTracker.switchToPhase(BlockPhase.State.BLOCK_DECAY, PhaseContext.start()
                    .add(NamedCause.source(spongeWorld.createSpongeBlockSnapshot(state, actualState, pos, 3)))
                    .addCaptures()
                    .complete());
        }
        boolean result = worldIn.setBlockState(pos, state, flags);
        if (isBlockAlready && !isWorldGen) {
            causeTracker.completePhase();
        }
        return result;
    }

    /**
     * @author gabizou - August 2nd, 2016
     * @reason Rewrite to handle both drops and the change state for leaves
     * that are considered to be decaying, so the drops do not leak into
     * whatever previous phase is being handled in. Since the issue is that
     * the block change takes place in a different phase (more than likely),
     * the drops are either "lost" or not considered for drops because the
     * blocks didn't change according to whatever previous phase.
     *
     * @param worldIn The world in
     * @param pos The position
     */
    @Overwrite
    private void destroy(World worldIn, BlockPos pos) {
        final IBlockState blockState = worldIn.getBlockState(pos);
        // Sponge Start - Cause tracking
        if (CauseTracker.ENABLED && !worldIn.isRemote) {
            final IMixinWorldServer mixinWorldServer = (IMixinWorldServer) worldIn;
            final CauseTracker causeTracker = mixinWorldServer.getCauseTracker();
            final PhaseData peek = causeTracker.getCurrentPhaseData();
            final IPhaseState currentState = peek.state;
            final boolean isWorldGen = currentState.getPhase().isWorldGeneration(currentState);
            final boolean isBlockAlready = causeTracker.getCurrentState().getPhase() != TrackingPhases.BLOCK;
            final IBlockState actualState = blockState.getActualState(worldIn, pos);
            if (isBlockAlready && !isWorldGen) {
                causeTracker.switchToPhase(BlockPhase.State.BLOCK_DECAY, PhaseContext.start()
                        .add(NamedCause.source(mixinWorldServer.createSpongeBlockSnapshot(blockState, actualState, pos, 3)))
                        .addCaptures()
                        .complete());
            }
            this.dropBlockAsItem(worldIn, pos, blockState, 0);
            worldIn.setBlockToAir(pos);
            if (isBlockAlready && !isWorldGen) {
                causeTracker.completePhase();
            }
            return;
        }
        // Sponge End
        this.dropBlockAsItem(worldIn, pos, blockState , 0);
        worldIn.setBlockToAir(pos);

    }

    protected ImmutableTreeData getTreeData(IBlockState blockState) {
        BlockPlanks.EnumType type;
        if (blockState.getBlock() instanceof BlockOldLeaf) {
            type = blockState.getValue(BlockOldLeaf.VARIANT);
        } else if (blockState.getBlock() instanceof BlockNewLeaf) {
            type = blockState.getValue(BlockNewLeaf.VARIANT);
        } else {
            type = BlockPlanks.EnumType.OAK;
        }

        final TreeType treeType = TreeTypeResolver.getFor(type);

        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeTreeData.class, treeType);
    }

    private ImmutableDecayableData getIsDecayableFor(IBlockState blockState) {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeDecayableData.class, blockState.getValue(BlockLeaves.DECAYABLE));
    }

    @Override
    public boolean supports(Class<? extends ImmutableDataManipulator<?, ?>> immutable) {
        return ImmutableTreeData.class.isAssignableFrom(immutable) || ImmutableDecayableData.class.isAssignableFrom(immutable);
    }

    @Override
    public Optional<BlockState> getStateWithData(IBlockState blockState, ImmutableDataManipulator<?, ?> manipulator) {
        if (manipulator instanceof ImmutableTreeData) {
            final TreeType treeType = ((ImmutableTreeData) manipulator).type().get();
            final BlockPlanks.EnumType type = TreeTypeResolver.getFor(treeType);
            if (blockState.getBlock() instanceof BlockOldLeaf) {
                if (treeType.equals(TreeTypes.OAK) ||
                        treeType.equals(TreeTypes.BIRCH) ||
                        treeType.equals(TreeTypes.SPRUCE) ||
                        treeType.equals(TreeTypes.JUNGLE)) {
                    return Optional.of((BlockState) blockState.withProperty(BlockOldLeaf.VARIANT, type));
                }
            } else if (blockState.getBlock() instanceof BlockNewLeaf) {
                if (treeType.equals(TreeTypes.ACACIA) || treeType.equals(TreeTypes.DARK_OAK)) {
                    return Optional.of((BlockState) blockState.withProperty(BlockNewLeaf.VARIANT, type));
                }
            }
            return Optional.empty();
        }
        if (manipulator instanceof ImmutableDecayableData) {
            final boolean decayable = ((ImmutableDecayableData) manipulator).decayable().get();
            return Optional.of((BlockState) blockState.withProperty(BlockLeaves.DECAYABLE, decayable));
        }
        return super.getStateWithData(blockState, manipulator);
    }

    @Override
    public <E> Optional<BlockState> getStateWithValue(IBlockState blockState, Key<? extends BaseValue<E>> key, E value) {
        if (key.equals(Keys.TREE_TYPE)) {
            final TreeType treeType = (TreeType) value;
            final BlockPlanks.EnumType type = TreeTypeResolver.getFor(treeType);
            if (blockState.getBlock() instanceof BlockOldLeaf) {
                if (treeType.equals(TreeTypes.OAK) ||
                        treeType.equals(TreeTypes.BIRCH) ||
                        treeType.equals(TreeTypes.SPRUCE) ||
                        treeType.equals(TreeTypes.JUNGLE)) {
                    return Optional.of((BlockState) blockState.withProperty(BlockOldLeaf.VARIANT, type));
                }
            } else if (blockState.getBlock() instanceof BlockNewLeaf) {
                if (treeType.equals(TreeTypes.ACACIA) || treeType.equals(TreeTypes.DARK_OAK)) {
                    return Optional.of((BlockState) blockState.withProperty(BlockNewLeaf.VARIANT, type));
                }
            }
            return Optional.empty();
        }
        if (key.equals(Keys.DECAYABLE)) {
            final boolean decayable = (Boolean) value;
            return Optional.of((BlockState) blockState.withProperty(BlockLeaves.DECAYABLE, decayable));
        }
        return super.getStateWithValue(blockState, key, value);
    }

    @Override
    public List<ImmutableDataManipulator<?, ?>> getManipulators(IBlockState blockState) {
        return ImmutableList.<ImmutableDataManipulator<?, ?>>of(getTreeData(blockState), getIsDecayableFor(blockState));

    }

}
