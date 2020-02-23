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
package org.spongepowered.common.mixin.tracker.world.chunk;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.bridge.OwnershipTrackedBridge;
import org.spongepowered.common.bridge.tileentity.TileEntityBridge;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.bridge.world.TrackedWorldBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.chunk.ActiveChunkReferantBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.bridge.world.chunk.TrackedChunkBridge;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.context.BlockTransaction;
import org.spongepowered.common.event.tracking.context.SpongeProxyBlockAccess;
import org.spongepowered.common.util.VecHelper;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Mixin(Chunk.class)
public abstract class ChunkMixin_Tracker implements TrackedChunkBridge {

    @Shadow @Final public static ChunkSection EMPTY_SECTION;
    @Shadow @Final private Map<BlockPos, TileEntity> tileEntities;
    @Shadow @Final private ChunkSection[] sections;
    @Shadow @Final private Map<Heightmap.Type, Heightmap> heightMap;
    @Shadow @Final private World world;
    @Shadow private volatile boolean dirty;

    @Shadow @Nullable public abstract TileEntity shadow$getTileEntity(BlockPos pos, Chunk.CreateEntityType creationMode);
    @Shadow public abstract BlockState getBlockState(BlockPos pos);

    /**
     * @author gabizou - January 13th, 2020 - Minecraft 1.14.3
     * @reason Reroute outsdie calls to chunk.setBlockState to flow through
     *  the tracker enhanced method.
     *
     * @param pos The position to set
     * @param state The state to set
     * @return The changed state
     */
    @Nullable
    @Overwrite
    public BlockState setBlockState(final BlockPos pos, final BlockState state, final boolean isMoving) {
        final BlockState iblockstate1 = this.getBlockState(pos);

        // Sponge - reroute to new method that accepts snapshot to prevent a second snapshot from being created.
        return this.bridge$setBlockState(pos, state, iblockstate1, BlockChangeFlags.ALL);
    }

    /**
     * @author gabizou - January 13th, 2020 - Minecraft 1.14.3
     * Technically a full overwrite for {@link Chunk#setBlockState(BlockPos, BlockState, boolean)}
     * and due to Sponge's hijacking of {@link ServerWorld#setBlockState(BlockPos, BlockState, int)},
     * it needs to be able to record transactions when necessary. This implementation allows for us to
     * further specify the types of transactions and what proxies are needing to set up where.
     *
     *
     * @param pos The position changing
     * @param newState The new state
     * @param currentState The current state - passed in from either chunk or world
     * @return The changed block state if not null
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    @Nullable
    public BlockState bridge$setBlockState(final BlockPos pos, final BlockState newState, final BlockState currentState, final BlockChangeFlag flag) {
        // int i = pos.getX() & 15;
        final int xPos = pos.getX() & 15;
        // int j = pos.getY();
        final int yPos = pos.getY();
        // int k = pos.getZ() & 15;
        final int zPos = pos.getZ() & 15;
        ChunkSection chunksection = this.sections[yPos >> 4];
        // if (chunksection == EMPTY_SECTION) { // Vanilla doesn't prefix but whatever.
        if (chunksection == ChunkMixin_Tracker.EMPTY_SECTION) {
            if (newState.isAir()) {
                return null;
            }

            chunksection = new ChunkSection(yPos >> 4 << 4);
            this.sections[yPos >> 4] = chunksection;
        }

        // boolean flag = chunksection.isEmpty();
        final boolean isEmpty = chunksection.isEmpty();

        // Sponge Start
        // Set up some default information variables for later processing
        final boolean isFake = ((WorldBridge) this.world).bridge$isFake();

        final TrackedWorldBridge trackedWorld = isFake ? null : (TrackedWorldBridge) this.world;

        final int modifiedY = yPos & 15;

        // Small optimization to avoid further state logic and only check if we need to update the proxies in play
        if (currentState == newState) {
            // Some micro optimization in case someone is trying to set the new state to the same as current
            if (trackedWorld != null) {
                final SpongeProxyBlockAccess proxyAccess = trackedWorld.bridge$getProxyAccess();
                if (proxyAccess.hasProxy() && proxyAccess.getBlockState(pos) != currentState) {
                    proxyAccess.onChunkChanged(pos, newState);
                }
                // NOTE: This is the ONLY time that we will ever return null forcibly without asking to
                // perform the state swap.
                return null;
            }

        }
        final TileEntity existing = this.shadow$getTileEntity(pos, Chunk.CreateEntityType.CHECK);
        final PhaseContext<?> peek = isFake ? null : PhaseTracker.getInstance().getCurrentContext();
        final IPhaseState state = isFake ? null : peek.state;
        final SpongeBlockSnapshot snapshot = (isFake
                || !ShouldFire.CHANGE_BLOCK_EVENT
                || !state.shouldCaptureBlockChangeOrSkip(peek, pos, currentState, newState, flag))
                ? null
                : this.tracker$createSpongeBlockSnapshot(currentState, currentState, pos, flag, existing);
        final BlockTransaction.ChangeBlock transaction;

        // BlockState blockstate = chunksection.setBlockState(xPos, yPos & 15, zPos, newState); // Vanilla
        final BlockState oldState = chunksection.setBlockState(xPos, modifiedY, zPos, newState);
        if (oldState == newState) {
            return null;
        }
        // Useless else block
        //} else {
        // Block block = newState.getBlock(); // Vanilla
        final Block newBlock = newState.getBlock();
        // Block block1 = blockstate.getBlock(); // Vanilla
        final Block oldBlock = oldState.getBlock();
        this.heightMap.get(Heightmap.Type.MOTION_BLOCKING).update(xPos, yPos, zPos, newState);
        this.heightMap.get(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES).update(xPos, yPos, zPos, newState);
        this.heightMap.get(Heightmap.Type.OCEAN_FLOOR).update(xPos, yPos, zPos, newState);
        this.heightMap.get(Heightmap.Type.WORLD_SURFACE).update(xPos, yPos, zPos, newState);
        // boolean flag1 = chunksection.isEmpty(); // Vanilla
        final boolean isSectionEmpty = chunksection.isEmpty();
        if (isEmpty != isSectionEmpty) {
            this.world.getChunkProvider().getLightManager().func_215567_a(pos, isSectionEmpty);
        }

        if (!this.world.isRemote) {
            // Sponge Start - If we're throwing events, need to register transactions and capture.
            // blockstate.onReplaced(this.world, pos, newState, isMoving); // Vanilla

            // Sponge - Redirect phase checks to use bridge$isFake in the event we have mods worlds doing silly things....
            // i.e. fake worlds. Likewise, avoid creating unnecessary snapshots/transactions
            // or triggering unprocessed captures when there are no events being thrown.
            if (!isFake && ShouldFire.CHANGE_BLOCK_EVENT && snapshot != null) {

                // Mark the tile entity as captured so when it is being removed during the chunk setting, it won't be
                // re-captured again.
                snapshot.blockChange = ((IPhaseState) peek.state).associateBlockChangeWithSnapshot(peek, newState, newBlock, currentState, snapshot, oldBlock);
                transaction = state.captureBlockChange(peek, pos, snapshot, newState, flag, existing);

                if (oldBlock != newBlock) {
                    // This is a deviation from Vanilla since we "optimize" the fact
                    // that the two blocks are the same, we don't need to call the
                    // "change". If any issues arise with mods (unlikely), this would be the if statement to "remove"
                    // or potentially check for the block or some configuration maybe...
                    // We want to queue the break logic later, while the transaction is processed
                    if (transaction != null) {
                        transaction.queueBreak = true;
                        transaction.enqueueChanges(trackedWorld.bridge$getProxyAccess(), peek.getCapturedBlockSupplier());
                    }
                    oldState.onReplaced(this.world, pos, newState, flag.isMoving());
                }
                if (existing != null && SpongeImplHooks.shouldRefresh(existing, this.world, pos, currentState, newState)) {
                    // And likewise, we want to queue the tile entity being removed, while
                    // the transaction is processed.
                    if (transaction != null) {
                        // Set tile to be captured, if it's showing up in removals later, it will
                        // be ignored since the transaction process will actually process
                        // the removal.
                        ((TileEntityBridge) existing).bridge$setCaptured(true);
                        transaction.queuedRemoval = existing;
                        transaction.enqueueChanges(trackedWorld.bridge$getProxyAccess(), peek.getCapturedBlockSupplier());
                    } else {
                        this.world.removeTileEntity(pos);
                    }
                }

            } else {
                transaction = null;
                // Sponge - Forge adds this change for block changes to only fire events when necessary
                if (oldBlock != newBlock && (state == null || !state.isRestoring())) { // cache the block break in the event we're capturing tiles
                    oldState.onReplaced(this.world, pos, currentState, flag.isMoving());
                }
                // Sponge - Add several tile entity hook checks. Mainly for forge added hooks, but these
                // still work by themselves in vanilla. In all intents and purposes, the remove tile entity could
                // occur before we have a chance to
                if (existing != null && SpongeImplHooks.shouldRefresh(existing, this.world, pos, currentState, newState)) {
                    this.world.removeTileEntity(pos);
                }
            }
            oldState.onReplaced(this.world, pos, newState, flag.isMoving());
            // Sponge End

        // } else if (oldBlock != newBlock && oldBlock instanceof ITileEntityProvider) { // Vanilla
        // Forge changes the check to blockState.hasTileEntity();
        } else {
            // Sponge set the transaction to null
            transaction = null;

            // Continue vanilla
            if (oldBlock != newBlock && SpongeImplHooks.hasBlockTileEntity(oldState)) {
                this.world.removeTileEntity(pos);
            }
        }

        // if (chunksection.getBlockState(xPos, yPos & 15, zPos).getBlock() != newBlock) { // Vanilla
        final BlockState blockAfterSet = chunksection.getBlockState(xPos, yPos & 15, zPos);
        if (blockAfterSet.getBlock() != newBlock) {
            // Sponge Start - prune tracked change
            if (!isFake && snapshot != null) {
                if (state.doesBulkBlockCapture(peek)) {
                    peek.getCapturedBlockSupplier().prune(snapshot);
                } else {
                    peek.setSingleSnapshot(null);
                }
            }
            // Sponge End
            return null;
        } // else { // Sponge - Redundant else

        // if (oldBlock instanceof ITileEntityProvider) { // Vanilla
        if (SpongeImplHooks.hasBlockTileEntity(oldState)) { // Forge changes to oldstate.hasTileEntity()
            // Sponge Start - We already retrieved the old tile entity
            // final TileEntity tileentity = this.shadow$getTileEntity(pos, Chunk.CreateEntityType.CHECK);
            // if (tileentity != null) {
            //    tileentity.updateContainingBlockInfo();
            // }
            if (existing != null) {
                existing.updateContainingBlockInfo();
            }
            // Sponge End
        }

        // Sponge Start - Handle block physics only if we're actually the server world
        // Vanilla calls the onBlockAdded call if it's a server, Sponge introduces an
        // additional physics check.
        // if (this.world.isRemote) {
        //     state.onBlockAdded(this.world, pos, blockstate, isMoving);
        // }
        if (!isFake) {
            if (currentState != newState) {
                // Reset the proxy access or add to the proxy state during processing.
                ((ServerWorldBridge) this.world).bridge$getProxyAccess().onChunkChanged(pos, newState);
            }
            final boolean isBulkCapturing = ShouldFire.CHANGE_BLOCK_EVENT && state != null && state.doesBulkBlockCapture(peek);
            // Ignore block activations during block placement captures unless it's
            // a BlockContainer. Prevents blocks such as TNT from activating when cancelled.
            // but, because we have transactions to deal with, we have to still set the transaction to
            // queue on add as long as the flag deems it so.
            if (flag.performBlockPhysics()) {
                // Occasionally, certain phase states will need to prevent onBlockAdded to be called until after the tile entity tracking
                // has been done, in the event of restores needing to re-override the block changes.
                if (transaction != null) {
                    transaction.queueOnAdd = true;
                } else if (!isBulkCapturing || SpongeImplHooks.hasBlockTileEntity(newState)) {
                    newState.onBlockAdded(this.world, pos, oldState, flag.isMoving());
                }
            }
            // Sponge end
        }

        // if (newBlock instanceof ITileEntityProvider) {
        if (SpongeImplHooks.hasBlockTileEntity(newState)) {
            // TileEntity tileentity1 = this.getTileEntity(pos, Chunk.CreateEntityType.CHECK);
            TileEntity newTileEntity = this.shadow$getTileEntity(pos, Chunk.CreateEntityType.CHECK);
            // Sponge Start - Additional check for the tile entity being queued for removal.
            // if (tileentity == null) { // Sponge
            if (newTileEntity == null || (transaction != null && transaction.queuedRemoval != null)) {
                // Vanilla uses the interface, Forge uses the direct method, we use SpongeImplHooks.
                // tileentity1 = ((ITileEntityProvider)block).createNewTileEntity(this.world);
                newTileEntity = SpongeImplHooks.createTileEntity(newState, this.world);
                if (!isFake) { // Surround with a server check
                    final User owner = peek.getOwner().orElse(null);
                    // If current owner exists, transfer it to newly created TE pos
                    // This is required for TE's that get created during move such as pistons and ComputerCraft turtles.
                    if (owner != null) {
                        ((ChunkBridge) this).bridge$addTrackedBlockPosition(newBlock, pos, owner, PlayerTracker.Type.OWNER);
                    }
                }
                if (transaction != null) {
                    // Go ahead and log the tile being replaced, the tile being removed will be at least already notified of removal
                    transaction.queueTileSet = newTileEntity;
                    if (newTileEntity != null) {
                        ((TileEntityBridge) newTileEntity).bridge$setCaptured(true);
                        newTileEntity.setWorld(this.world);
                        newTileEntity.setPos(pos);// Set the position
                    }
                    transaction.enqueueChanges(trackedWorld.bridge$getProxyAccess(), peek.getCapturedBlockSupplier());
                } else {
                    // Some mods are relying on the world being set prior to setting the tile
                    // world prior to the position. It's weird, but during block restores, this can
                    // cause an exception.
                    // See https://github.com/SpongePowered/SpongeForge/issues/2677 for reference.
                    // Note that vanilla will set the world later, and forge sets the world
                    // after setting the position, but a mod has expectations that defy both of these...
                    if (newTileEntity != null && newTileEntity.getWorld() != this.world) {
                        newTileEntity.setWorld(this.world);
                    }
                    this.world.setTileEntity(pos, newTileEntity);
                }
            } else {
                newTileEntity.updateContainingBlockInfo();
            }
        }
        if (transaction != null) {
            // We still want to enqueue any changes to the transaction, including any tiles removed
            // if there was a tile entity added, it will be logged above
            transaction.enqueueChanges(trackedWorld.bridge$getProxyAccess(), peek.getCapturedBlockSupplier());
        }

        this.dirty = true;
        return oldState;
    }

    private SpongeBlockSnapshot tracker$createSpongeBlockSnapshot(
            final BlockState state, final BlockState extended, final BlockPos pos, final BlockChangeFlag updateFlag, @Nullable final TileEntity existing) {
        final SpongeBlockSnapshotBuilder builder = SpongeBlockSnapshotBuilder.pooled();
        builder.reset();
        builder.blockState(state)
                .worldId(((WorldProperties) this.world.getWorldInfo()).getUniqueId())
                .position(VecHelper.toVector3i(pos));
        final Optional<UUID> creator = ((ChunkBridge) this).bridge$getBlockOwnerUUID(pos);
        final Optional<UUID> notifier = ((ChunkBridge) this).bridge$getBlockNotifierUUID(pos);
        creator.ifPresent(builder::creator);
        notifier.ifPresent(builder::notifier);
        if (existing != null) {
            TrackingUtil.addTileEntityToBuilder(existing, builder);
        }
        builder.flag(updateFlag);
        return builder.build();
    }


    @Override
    public void bridge$removeTileEntity(final TileEntity removed) {
        final TileEntity tileentity = this.tileEntities.remove(removed.getPos());
        if (tileentity != removed && tileentity != null) {
            // Because multiple requests to remove a tile entity could cause for checks
            // without actually knowing if the chunk doesn't have the tile entity, this
            // avoids storing nulls.
            // Replace the pre-existing tile entity in case we remove a tile entity
            // we don't want to be removing.
            this.tileEntities.put(removed.getPos(), tileentity);
        }
        ((ActiveChunkReferantBridge) removed).bridge$setActiveChunk(null);
        removed.remove();
    }

    @Override
    public void bridge$setTileEntity(final BlockPos pos, final TileEntity added) {
        if (added.getWorld() != this.world) {
            // Forge adds this because some mods do stupid things....
            added.setWorld(this.world);
        }
        added.setPos(pos);
        if (this.tileEntities.containsKey(pos)) {
            this.tileEntities.get(pos).remove();
        }
        added.validate();
        ((ActiveChunkReferantBridge) added).bridge$setActiveChunk((ChunkBridge) this);
        this.tileEntities.put(pos, added);
    }

    @Inject(
            method = "addTileEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/tileentity/TileEntity;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;validate()V"))
    private void tracker$SetActiveChunkOnTileEntityAdd(final BlockPos pos, final TileEntity tileEntityIn, final CallbackInfo ci) {
        ((ActiveChunkReferantBridge) tileEntityIn).bridge$setActiveChunk((ChunkBridge) this);
        // Make sure to set owner/notifier for TE if any chunk data exists
        // Failure to do this during chunk load will cause TE's to not have proper user tracking
        ((OwnershipTrackedBridge) tileEntityIn).tracked$setTrackedUUID(PlayerTracker.Type.NOTIFIER, ((ChunkBridge) this).bridge$getBlockNotifierUUID(pos).orElse(null));
        ((OwnershipTrackedBridge) tileEntityIn).tracked$setTrackedUUID(PlayerTracker.Type.OWNER, ((ChunkBridge) this).bridge$getBlockOwnerUUID(pos).orElse(null));
    }

}
