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
package org.spongepowered.common.mixin.core.world;

import static com.google.common.base.Preconditions.checkArgument;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.PositionOutOfBoundsException;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.event.CauseTracker;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;
import org.spongepowered.common.util.StaticMixinHelper;
import org.spongepowered.common.world.CaptureType;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

@Mixin(value = net.minecraft.world.World.class, priority = 1001)
public abstract class MixinWorld_Tracker implements World, IMixinWorld {

    private static final Vector3i BLOCK_MIN = new Vector3i(-30000000, 0, -30000000);
    private static final Vector3i BLOCK_MAX = new Vector3i(30000000, 256, 30000000).sub(1, 1, 1);
    @SuppressWarnings("unused")
    private static final Vector2i BIOME_MIN = BLOCK_MIN.toVector2(true);
    @SuppressWarnings("unused")
    private static final Vector2i BIOME_MAX = BLOCK_MAX.toVector2(true);

    private final CauseTracker causeTracker = new CauseTracker((net.minecraft.world.World) (Object) this);
    private final Map<net.minecraft.entity.Entity, Vector3d> rotationUpdates = new HashMap<>();

    @Shadow @Final public boolean isRemote;
    @Shadow @Final public Profiler theProfiler;
    @Shadow protected WorldInfo worldInfo;

    @Shadow public abstract boolean isValid(BlockPos pos);
    @Shadow public abstract void markBlockForUpdate(BlockPos pos);
    @Shadow public abstract void notifyNeighborsRespectDebug(BlockPos pos, Block blockType);
    @Shadow public abstract net.minecraft.world.chunk.Chunk getChunkFromBlockCoords(BlockPos pos);
    @Shadow public abstract boolean checkLight(BlockPos pos);


    @Inject(method = "<init>", at = @At("RETURN"))
    private void onTrackerConstructed(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client,
            CallbackInfo ci) {

        // Turn on capturing
        final CauseTracker causeTracker = this.getCauseTracker();
        causeTracker.setCaptureBlocks(true);
        causeTracker.setCaptureEntitySpawns(true);
    }

    /**
     * @author bloodmc
     *
     * Purpose: Rewritten to support capturing blocks
     */
    @Overwrite
    public boolean setBlockState(BlockPos pos, IBlockState newState, int flags) {
        if (!this.isValid(pos)) {
            return false;
        } else if (!this.isRemote && this.worldInfo.getTerrainType() == WorldType.DEBUG_WORLD) {
            return false;
        } else {
            net.minecraft.world.chunk.Chunk chunk = this.getChunkFromBlockCoords(pos);
            IBlockState currentState = chunk.getBlockState(pos);
            if (currentState == newState) {
                return false;
            }

            Block block = newState.getBlock();
            BlockSnapshot originalBlockSnapshot = null;
            BlockSnapshot newBlockSnapshot = null;

            // Don't capture if we are restoring blocks
            final CauseTracker causeTracker = this.getCauseTracker();
            if (!this.isRemote && !causeTracker.isRestoringBlocks() && !causeTracker.isWorldSpawnerRunning() && !causeTracker.isChunkSpawnerRunning()
                    && !causeTracker.isCapturingTerrainGen()) {
                originalBlockSnapshot = null;
                originalBlockSnapshot = createSpongeBlockSnapshot(currentState, currentState.getBlock().getActualState(currentState,
                        (IBlockAccess) this, pos), pos, flags);

                if (causeTracker.isCaptureBlockDecay()) {
                    // Only capture final state of decay, ignore the rest
                    if (block == Blocks.air) {
                        ((SpongeBlockSnapshot) originalBlockSnapshot).captureType = CaptureType.DECAY;
                        causeTracker.getCapturedSpongeBlockSnapshots().add(originalBlockSnapshot);
                    }
                } else if (block == Blocks.air) {
                    ((SpongeBlockSnapshot) originalBlockSnapshot).captureType = CaptureType.BREAK;
                    causeTracker.getCapturedSpongeBlockSnapshots().add(originalBlockSnapshot);
                } else if (block != currentState.getBlock()) {
                    ((SpongeBlockSnapshot) originalBlockSnapshot).captureType = CaptureType.PLACE;
                    causeTracker.getCapturedSpongeBlockSnapshots().add(originalBlockSnapshot);
                } else {
                    ((SpongeBlockSnapshot) originalBlockSnapshot).captureType = CaptureType.MODIFY;
                    causeTracker.getCapturedSpongeBlockSnapshots().add(originalBlockSnapshot);
                }
            }

            int oldLight = currentState.getBlock().getLightValue();

            IBlockState iblockstate1 = ((IMixinChunk) chunk).setBlockState(pos, newState, currentState, newBlockSnapshot);

            if (iblockstate1 == null) {
                if (originalBlockSnapshot != null) {
                    causeTracker.getCapturedSpongeBlockSnapshots().remove(originalBlockSnapshot);
                }
                return false;
            } else {
                Block block1 = iblockstate1.getBlock();

                if (block.getLightOpacity() != block1.getLightOpacity() || block.getLightValue() != oldLight) {
                    this.theProfiler.startSection("checkLight");
                    this.checkLight(pos);
                    this.theProfiler.endSection();
                }

                if (causeTracker.hasPluginCause()) {
                    causeTracker.handleBlockCaptures(causeTracker.getPluginCause().get());
                } else {
                    // Don't notify clients or update physics while capturing blockstates
                    if (originalBlockSnapshot == null) {
                        // Modularize client and physic updates
                        markAndNotifyNeighbors(pos, chunk, iblockstate1, newState, flags);
                    }
                }

                return true;
            }
        }
    }


    @Override
    public void addEntityRotationUpdate(net.minecraft.entity.Entity entity, Vector3d rotation) {
        this.rotationUpdates.put(entity, rotation);
    }

    private void updateRotation(net.minecraft.entity.Entity entityIn) {
        Vector3d rotationUpdate = this.rotationUpdates.get(entityIn);
        if (rotationUpdate != null) {
            entityIn.rotationPitch = (float) rotationUpdate.getX();
            entityIn.rotationYaw = (float) rotationUpdate.getY();
        }
        this.rotationUpdates.remove(entityIn);
    }


    @Override
    public void markAndNotifyNeighbors(BlockPos pos, @Nullable net.minecraft.world.chunk.Chunk chunk, IBlockState old, IBlockState new_, int flags) {
        if ((flags & 2) != 0 && (!this.isRemote || (flags & 4) == 0) && (chunk == null || chunk.isPopulated())) {
            this.markBlockForUpdate(pos);
        }

        if (!this.isRemote && (flags & 1) != 0) {
            this.notifyNeighborsRespectDebug(pos, old.getBlock());

            if (new_.getBlock().hasComparatorInputOverride()) {
                this.updateComparatorOutputLevel(pos, new_.getBlock());
            }
        }
    }

    @Redirect(method = "forceBlockUpdateTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;updateTick(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V"))
    public void onForceBlockUpdateTick(Block block, net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, Random rand) {
        final CauseTracker causeTracker = this.getCauseTracker();
        if (this.isRemote || causeTracker.hasTickingBlock() || causeTracker.isCapturingTerrainGen()
            || causeTracker.isWorldSpawnerRunning() || causeTracker.isChunkSpawnerRunning()) {
            block.updateTick(worldIn, pos, state, rand);
            return;
        }

        causeTracker.setProcessingCaptureCause(true);
        causeTracker.setCurrentTickBlock(createSpongeBlockSnapshot(state, state.getBlock().getActualState(state, (IBlockAccess) this, pos), pos, 0));
        block.updateTick(worldIn, pos, state, rand);
        causeTracker.handlePostTickCaptures(Cause.of(NamedCause.source(causeTracker.getCurrentTickBlock().get())));
        causeTracker.setCurrentTickBlock(null);
        causeTracker.setProcessingCaptureCause(false);
    }

    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;onUpdate()V"))
    public void onUpdateEntities(net.minecraft.entity.Entity entityIn) {
        final CauseTracker causeTracker = this.getCauseTracker();
        if (this.isRemote || causeTracker.hasTickingEntity()) {
            entityIn.onUpdate();
            return;
        }

        causeTracker.setProcessingCaptureCause(true);
        causeTracker.setCurrentTickEntity((Entity) entityIn);
        entityIn.onUpdate();
        updateRotation(entityIn);
        SpongeCommonEventFactory.handleEntityMovement(entityIn);
        causeTracker.handlePostTickCaptures(Cause.of(NamedCause.source(entityIn)));
        causeTracker.setCurrentTickEntity(null);
        causeTracker.setProcessingCaptureCause(false);
    }

    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ITickable;update()V"))
    public void onUpdateTileEntities(ITickable tile) {
        final CauseTracker causeTracker = this.getCauseTracker();
        if (this.isRemote || causeTracker.hasTickingTileEntity()) {
            tile.update();
            return;
        }

        causeTracker.setProcessingCaptureCause(true);
        causeTracker.setCurrentTickTileEntity((TileEntity) tile);
        tile.update();
        causeTracker.handlePostTickCaptures(Cause.of(NamedCause.source(tile)));
        causeTracker.setCurrentTickTileEntity(null);
        causeTracker.setProcessingCaptureCause(false);
    }

    @Redirect(method = "updateEntityWithOptionalForce", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;onUpdate()V"))
    public void onCallEntityUpdate(net.minecraft.entity.Entity entity) {
        final CauseTracker causeTracker = this.getCauseTracker();
        if (this.isRemote || causeTracker.hasTickingEntity() || StaticMixinHelper.packetPlayer != null) {
            entity.onUpdate();
            return;
        }

        causeTracker.setProcessingCaptureCause(true);
        causeTracker.setCurrentTickEntity((Entity) entity);
        entity.onUpdate();
        updateRotation(entity);
        SpongeCommonEventFactory.handleEntityMovement(entity);
        causeTracker.handlePostTickCaptures(Cause.of(NamedCause.source(entity)));
        causeTracker.setCurrentTickEntity(null);
        causeTracker.setProcessingCaptureCause(false);
    }

    /**
     * @author bloodmc
     *
     * Purpose: Redirects vanilla method to our method which includes a cause.
     */
    @Overwrite
    public boolean spawnEntityInWorld(net.minecraft.entity.Entity entity) {
        return spawnEntity((Entity) entity, Cause.of(NamedCause.source(this)));
    }

    @Override
    public boolean spawnEntity(Entity entity, Cause cause) {
        return this.getCauseTracker().processSpawnEntity(entity, cause);
    }

    /**
     * @author bloodmc - November 15th, 2015
     *
     * Purpose: Rewritten to pass the source block position.
     */
    @Overwrite
    public void notifyNeighborsOfStateChange(BlockPos pos, Block blockType) {
        if (!isValid(pos)) {
            return;
        }

        final CauseTracker causeTracker = this.getCauseTracker();
        if (this.isRemote) {
            for (EnumFacing facing : EnumFacing.values()) {
                causeTracker.notifyBlockOfStateChange(pos.offset(facing), blockType, pos);
            }
            return;
        }

        NotifyNeighborBlockEvent
                event =
                SpongeCommonEventFactory.callNotifyNeighborEvent(this, pos, java.util.EnumSet.allOf(EnumFacing.class));
        if (event.isCancelled()) {
            return;
        }

        for (EnumFacing facing : EnumFacing.values()) {
            if (event.getNeighbors().keySet().contains(DirectionFacingProvider.getInstance().getKey(facing).get())) {
                causeTracker.notifyBlockOfStateChange(pos.offset(facing), blockType, pos);
            }
        }
    }

    /**
     * @author bloodmc - November 15th, 2015
     *
     * Purpose: Rewritten to pass the source block position.
     */
    @SuppressWarnings("rawtypes")
    @Overwrite
    public void notifyNeighborsOfStateExcept(BlockPos pos, Block blockType, EnumFacing skipSide) {
        if (!isValid(pos)) {
            return;
        }

        EnumSet directions = EnumSet.allOf(EnumFacing.class);
        directions.remove(skipSide);

        final CauseTracker causeTracker = this.getCauseTracker();
        if (this.isRemote) {
            for (Object obj : directions) {
                EnumFacing facing = (EnumFacing) obj;
                causeTracker.notifyBlockOfStateChange(pos.offset(facing), blockType, pos);
            }
            return;
        }

        NotifyNeighborBlockEvent event = SpongeCommonEventFactory.callNotifyNeighborEvent(this, pos, directions);
        if (event.isCancelled()) {
            return;
        }

        for (EnumFacing facing : EnumFacing.values()) {
            if (event.getNeighbors().keySet().contains(DirectionFacingProvider.getInstance().getKey(facing).get())) {
                causeTracker.notifyBlockOfStateChange(pos.offset(facing), blockType, pos);
            }
        }
    }

    /**
     * @author bloodmc - November 15th, 2015
     *
     * Purpose: Redirect's vanilla method to ours that includes source block
     * position.
     */
    @Overwrite
    public void notifyBlockOfStateChange(BlockPos notifyPos, final Block blockIn) {
        this.getCauseTracker().notifyBlockOfStateChange(notifyPos, blockIn, null);
    }

    /**
     * @author bloodmc - November 15th, 2015
     *
     * Purpose: Used to track comparators when they update levels.
     */
    @Overwrite
    public void updateComparatorOutputLevel(BlockPos pos, Block blockIn) {
        SpongeImplHooks.updateComparatorOutputLevel((net.minecraft.world.World) (Object) this, pos, blockIn);
    }

    @Override
    public CauseTracker getCauseTracker() {
        return this.causeTracker;
    }


    @Override
    public void setBlock(int x, int y, int z, BlockState block, boolean notifyNeighbors) {
        this.getCauseTracker().setPluginCause(null);
        checkBlockBounds(x, y, z);
        setBlockState(new BlockPos(x, y, z), (IBlockState) block, notifyNeighbors ? 3 : 2);
    }

    @Override
    public void setBlock(int x, int y, int z, BlockState blockState, boolean notifyNeighbors, Cause cause) {
        checkArgument(cause != null, "Cause cannot be null!");
        checkArgument(cause.root() instanceof PluginContainer, "PluginContainer must be at the ROOT of a cause!");
        final CauseTracker causeTracker = this.getCauseTracker();
        causeTracker.setPluginCause(cause);
        checkBlockBounds(x, y, z);
        setBlockState(new BlockPos(x, y, z), (IBlockState) blockState, notifyNeighbors ? 3 : 2);
        causeTracker.setPluginCause(null);
    }

    private void checkBlockBounds(int x, int y, int z) {
        if (!containsBlock(x, y, z)) {
            throw new PositionOutOfBoundsException(new Vector3i(x, y, z), BLOCK_MIN, BLOCK_MAX);
        }
    }

    @SuppressWarnings("unused")
    private net.minecraft.world.World asMinecraftWorld() {
        return (net.minecraft.world.World) (Object) this;
    }
}
