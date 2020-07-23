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
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.bridge.block.BlockStateBridge;
import org.spongepowered.common.bridge.tileentity.TrackableTileEntityBridge;
import org.spongepowered.common.bridge.world.TrackedWorldBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.chunk.ActiveChunkReferantBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.bridge.world.chunk.TrackedChunkBridge;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.context.BlockTransaction;
import org.spongepowered.common.event.tracking.context.SpongeProxyBlockAccess;
import org.spongepowered.common.world.SpongeBlockChangeFlag;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(Chunk.class)
public abstract class ChunkMixin_Tracker implements TrackedChunkBridge {
    // @formatter:off
    @Shadow @Final public static ChunkSection EMPTY_SECTION;
    @Shadow @Final private Map<BlockPos, TileEntity> tileEntities;
    @Shadow @Final private ChunkSection[] sections;
    @Shadow @Final private Map<Heightmap.Type, Heightmap> heightMap;
    @Shadow @Final private World world;
    @Shadow private volatile boolean dirty;

    @Shadow @Nullable public abstract TileEntity shadow$getTileEntity(BlockPos pos, Chunk.CreateEntityType creationMode);
    @Shadow public abstract BlockState getBlockState(BlockPos pos);
    // @formatter:on

//    /**
//     * @param pos The position to set
//     * @param state The state to set
//     * @return The changed state
//     * @author gabizou - January 13th, 2020 - Minecraft 1.14.3
//     * @reason Reroute outsdie calls to chunk.setBlockState to flow through
//     *         the tracker enhanced method.
//     */
//    @Nullable
//    @Overwrite
//    public BlockState setBlockState(final BlockPos pos, final BlockState state, final boolean isMoving) {
//        final BlockState iblockstate1 = this.getBlockState(pos);
//
//        // Sponge - reroute to new method that accepts snapshot to prevent a second snapshot from being created.
//        return this.bridge$setBlockState(pos, state, iblockstate1, BlockChangeFlags.ALL);
//    }

    /**
     * @param pos The position changing
     * @param newState The new state
     * @param currentState The current state - passed in from either chunk or world
     * @return The changed block state if not null
     * @author gabizou - January 13th, 2020 - Minecraft 1.14.3
     *         Technically a full overwrite for {@link Chunk#setBlockState(BlockPos, BlockState, boolean)}
     *         and due to Sponge's hijacking of {@link ServerWorld#setBlockState(BlockPos, BlockState, int)},
     *         it needs to be able to record transactions when necessary. This implementation allows for us to
     *         further specify the types of transactions and what proxies are needing to set up where.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    @Nullable
    public BlockState bridge$setBlockState(final BlockPos pos, final BlockState newState, final BlockState currentState,
            final BlockChangeFlag flag) {
        // int i = pos.getX() & 15;
        final int xPos = pos.getX() & 15;
        // int j = pos.getY();
        final int yPos = pos.getY();
        // int k = pos.getZ() & 15;
        final int zPos = pos.getZ() & 15;
        // Sponge - get the moving flag from our flag construct
        final boolean isMoving = ((SpongeBlockChangeFlag) flag).isBlockMoving();
        ChunkSection chunksection = this.sections[yPos >> 4];
        if (chunksection == EMPTY_SECTION) {
            if (newState.isAir()) {
                return null;
            }

            chunksection = new ChunkSection(yPos >> 4 << 4);
            this.sections[yPos >> 4] = chunksection;
        }

        // boolean flag = chunksection.isEmpty(); // Vanilla flag -> isEmpty
        final boolean isEmpty = chunksection.isEmpty();
        // BlockState blockstate = chunksection.setBlockState(xPos, yPos & 15, zPos, newState); // blockstate -> oldState
        final BlockState oldState = chunksection.setBlockState(xPos, yPos & 15, zPos, newState);
        if (oldState == newState) {
            return null;
        } else {
            // Block block = state.getBlock(); // Vanilla block -> newBlock
            final Block newBlock = newState.getBlock();

            // Block block1 = blockstate.getBlock(); // Vanilla block1 -> oldBlock
            final Block oldBlock = oldState.getBlock();

            this.heightMap.get(Heightmap.Type.MOTION_BLOCKING).update(xPos, yPos, zPos, newState);
            this.heightMap.get(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES).update(xPos, yPos, zPos, newState);
            this.heightMap.get(Heightmap.Type.OCEAN_FLOOR).update(xPos, yPos, zPos, newState);
            this.heightMap.get(Heightmap.Type.WORLD_SURFACE).update(xPos, yPos, zPos, newState);
            final boolean isChunkStillEmpty = chunksection.isEmpty();
            if (isEmpty != isChunkStillEmpty) {
                this.world.getChunkProvider().getLightManager().func_215567_a(pos, isChunkStillEmpty);
            }

            if (!this.world.isRemote) {
                oldState.onReplaced(this.world, pos, newState, isMoving);
            } else if (this.bridge$shouldRefreshTile(oldBlock, newBlock, oldState, newState)) {
                this.world.removeTileEntity(pos);
            }

            if (chunksection.getBlockState(xPos, yPos & 15, zPos).getBlock() != newBlock) {
                return null;
            } else {
                // if (block1 instanceof ITileEntityProvider) { // Vanilla
                // if (blockstate.hasTileEntity()) { // Forge
                // we bridge the differences here with a bridge method
                if (((BlockStateBridge) oldState).bridge$hasTileEntity()) {
                    final TileEntity tileentity = this.shadow$getTileEntity(pos, Chunk.CreateEntityType.CHECK);
                    if (tileentity != null) {
                        tileentity.updateContainingBlockInfo();
                    }
                }

                if (!this.world.isRemote) {
                    newState.onBlockAdded(this.world, pos, oldState, isMoving);
                }

                // if (newBlock instanceof ITileEntityProvider) { // Vanilla
                // if (newState.hasTileEntity()) { // Forge
                // We again cast to the bridge for easy access
                if (((BlockStateBridge) newState).bridge$hasTileEntity()) {
                    TileEntity tileentity1 = this.shadow$getTileEntity(pos, Chunk.CreateEntityType.CHECK);
                    if (tileentity1 == null) {
                        // tileentity1 = ((ITileEntityProvider)block).createNewTileEntity(this.world); // Vanilla
                        // tileentity1 = state.createTileEntity(this.world); // Forge
                        // We cast to our bridge for easy access
                        tileentity1 = ((BlockStateBridge) newState).bridge$createNewTileEntity(this.world);
                        this.world.setTileEntity(pos, tileentity1);
                    } else {
                        tileentity1.updateContainingBlockInfo();
                    }
                }

                this.dirty = true;
                return oldState;
            }
        }
    }

    @Inject(method = "addEntity", at = @At("RETURN"))
    private void tracker$SetActiveChunkOnEntityAdd(final Entity entityIn, final CallbackInfo ci) {
        ((ActiveChunkReferantBridge) entityIn).bridge$setActiveChunk(this);
    }

    @Inject(method = "removeEntityAtIndex", at = @At("RETURN"))
    private void tracker$ResetEntityActiveChunk(final Entity entityIn, final int index, final CallbackInfo ci) {
        ((ActiveChunkReferantBridge) entityIn).bridge$setActiveChunk(null);
    }
//
//    @Redirect(method = "removeTileEntity",
//        at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;remove()V"))
//    private void tracker$resetTileEntityActiveChunk(final TileEntity tileEntityIn) {
//        ((ActiveChunkReferantBridge) tileEntityIn).bridge$setActiveChunk(null);
//        tileEntityIn.remove();
//    }
//
//    @Override
//    public void bridge$removeTileEntity(final TileEntity removed) {
//        final TileEntity tileentity = this.tileEntities.remove(removed.getPos());
//        if (tileentity != removed && tileentity != null) {
//            // Because multiple requests to remove a tile entity could cause for checks
//            // without actually knowing if the chunk doesn't have the tile entity, this
//            // avoids storing nulls.
//            // Replace the pre-existing tile entity in case we remove a tile entity
//            // we don't want to be removing.
//            this.tileEntities.put(removed.getPos(), tileentity);
//        }
//        ((ActiveChunkReferantBridge) removed).bridge$setActiveChunk(null);
//        removed.remove();
//    }

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
        ((ActiveChunkReferantBridge) added).bridge$setActiveChunk(this);
        this.tileEntities.put(pos, added);
    }

    @Inject(
            method = "addTileEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/tileentity/TileEntity;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;validate()V"))
    private void tracker$SetActiveChunkOnTileEntityAdd(final BlockPos pos, final TileEntity tileEntityIn, final CallbackInfo ci) {
        ((ActiveChunkReferantBridge) tileEntityIn).bridge$setActiveChunk(this);
        // Make sure to set creator/notifier for TE if any chunk data exists
        // Failure to do this during chunk load will cause TE's to not have proper user tracking
        // TODO - Reimplement player uuid tracking.
//        ((CreatorTrackedBridge) tileEntityIn).tracked$setTrackedUUID(PlayerTracker.Type.CREATOR, ((ChunkBridge) this).bridge$getBlockCreatorUUID(pos).orElse(null));
//        ((CreatorTrackedBridge) tileEntityIn).tracked$setTrackedUUID(PlayerTracker.Type.NOTIFIER, null);
    }


    @Inject(method = "getEntitiesWithinAABBForEntity", at = @At("RETURN"))
    private void tracker$ThrowCollisionEvent(final Entity entityIn, final AxisAlignedBB aabb, final List<Entity> listToFill,
            final java.util.function.Predicate<? super Entity> filter, final CallbackInfo ci) {
        if (((WorldBridge) this.world).bridge$isFake() || PhaseTracker.getInstance().getCurrentState().ignoresEntityCollisions()) {
            return;
        }

        if (listToFill.isEmpty()) {
            return;
        }

        if (!ShouldFire.COLLIDE_ENTITY_EVENT) {
            return;
        }

        final CollideEntityEvent event = SpongeCommonEventFactory.callCollideEntityEvent(this.world, entityIn, listToFill);

        if (event == null || event.isCancelled()) {
            if (event == null && !PhaseTracker.getInstance().getCurrentState().isTicking()) {
                return;
            }
            listToFill.clear();
        }
    }

//    @Inject(method = "getEntitiesOfTypeWithinAABB", at = @At("RETURN"))
//    private <T extends Entity> void tracker$throwCollsionEvent(final Class<? extends T> entityClass,
//            final AxisAlignedBB aabb, final List<T> listToFill, final Predicate<? super T> filter,
//            final CallbackInfo ci) {
//        if (((WorldBridge) this.world).bridge$isFake()
//                || PhaseTracker.getInstance().getCurrentState().ignoresEntityCollisions()) {
//            return;
//        }
//
//        if (listToFill.isEmpty()) {
//            return;
//        }
//
//        final CollideEntityEvent event = SpongeCommonEventFactory.callCollideEntityEvent(this.world, null, listToFill);
//
//        if (event == null || event.isCancelled()) {
//            if (event == null && !PhaseTracker.getInstance().getCurrentState().isTicking()) {
//                return;
//            }
//            listToFill.clear();
//        }
//    }
}
