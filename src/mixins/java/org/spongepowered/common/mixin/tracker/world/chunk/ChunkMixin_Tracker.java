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
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.Level;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.chunk.ActiveChunkReferantBridge;
import org.spongepowered.common.bridge.world.chunk.TrackedChunkBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.context.transaction.BlockTransaction;
import org.spongepowered.common.event.tracking.context.transaction.pipeline.ChunkPipeline;
import org.spongepowered.common.util.PrettyPrinter;
import org.spongepowered.common.world.SpongeBlockChangeFlag;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


@Mixin(Chunk.class)
public abstract class ChunkMixin_Tracker implements TrackedChunkBridge {
    // @formatter:off
    @Shadow @Final public static ChunkSection EMPTY_SECTION;
    @Shadow @Final private Map<BlockPos, TileEntity> tileEntities;
    @Shadow @Final private ChunkSection[] sections;
    @Shadow @Final private Map<Heightmap.Type, Heightmap> heightMap;
    @Shadow @Final private World world;
    @Shadow private volatile boolean dirty;

    @Shadow public abstract @Nullable TileEntity shadow$getTileEntity(BlockPos pos, Chunk.CreateEntityType creationMode);
    @Shadow public abstract BlockState getBlockState(BlockPos pos);
    // @formatter:on

    @Inject(method = "setBlockState", at = @At("HEAD"), cancellable = true)
    private void tracker$sanityCheckServerWorldSetBlockState(final BlockPos pos, final BlockState state, final boolean isMoving,
        final CallbackInfoReturnable<BlockState> cir) {
        if (!((WorldBridge) this.world).bridge$isFake()) {
            new PrettyPrinter(80).add("Illegal Direct Chunk Access")
                .hr()
                .add(new IllegalAccessException("No one should be accessing Chunk.setBlockState in a ServerWorld's environment"))
                .log(PhaseTracker.LOGGER, Level.WARN);
            cir.setReturnValue(null);
        }
    }

    /**
     * @param pos The position changing
     * @param newState The new state
     * @param currentState The current state - passed in from either chunk or world
     * @param flag
     * @return The changed block state if not null
     * @author gabizou - January 13th, 2020 - Minecraft 1.14.3
     *         Technically a full overwrite for {@link Chunk#setBlockState(BlockPos, BlockState, boolean)}
     *         and due to Sponge's hijacking of {@link ServerWorld#setBlockState(BlockPos, BlockState, int)},
     *         it needs to be able to record transactions when necessary. This implementation allows for us to
     *         further specify the types of transactions and what proxies are needing to set up where.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public ChunkPipeline bridge$createChunkPipeline(final BlockPos pos, final BlockState newState, final BlockState currentState,
        final SpongeBlockChangeFlag flag
    ) {
        final boolean isFake = ((WorldBridge) this.world).bridge$isFake();
        if (isFake) {
            throw new IllegalStateException("Cannot call ChunkBridge.bridge$buildChunkPipeline in non-Server managed worlds");
        }
        // int i = pos.getX() & 15;
        final int xPos = pos.getX() & 15;
        // int j = pos.getY();
        final int yPos = pos.getY();
        // int k = pos.getZ() & 15;
        final int zPos = pos.getZ() & 15;
        // Sponge - get the moving flag from our flag construct
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
        // Sponge Start - Build out the BlockTransaction
        final PhaseContext<?> context = PhaseTracker.getInstance().getPhaseContext();
        final IPhaseState state = context.state;
        final @Nullable TileEntity existing = this.shadow$getTileEntity(pos, Chunk.CreateEntityType.CHECK);
        // Build a transaction maybe?
        final WeakReference<ServerWorld> ref = new WeakReference<>((ServerWorld) this.world);
        final SpongeBlockSnapshot snapshot = TrackingUtil.createPooledSnapshot(currentState, pos, flag, existing,
                () -> Objects.requireNonNull(ref.get(), "ServerWorld dereferenced"),
                Optional::empty, Optional::empty);

        // Pulled up from below
        final Block newBlock = newState.getBlock();
        final Block currentBlock = currentState.getBlock();

        final BlockTransaction.ChangeBlock transaction = state.createTransaction(context, pos, snapshot, newState, flag, existing);

        snapshot.blockChange = state.associateBlockChangeWithSnapshot(
            context,
            newState,
            newBlock,
            currentState,
            snapshot,
            currentBlock
        );

        final ChunkPipeline.Builder builder = ChunkPipeline.builder()
            .kickOff(transaction)
            .chunk((Chunk) (Object) this)
            .chunkSection(chunksection)
            .world((ServerWorld) this.world);

        // Populate the effects
        transaction.populateChunkEffects(context.getBlockTransactor(), builder, chunksection);

        return builder.build();
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
