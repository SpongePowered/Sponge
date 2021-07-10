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
package org.spongepowered.common.mixin.tracker.world.level.chunk;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoTickList;
import net.minecraft.world.phys.AABB;
import org.apache.logging.log4j.Level;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.bridge.CreatorTrackedBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.level.block.state.BlockStateBridge;
import org.spongepowered.common.bridge.world.level.chunk.ActiveChunkReferantBridge;
import org.spongepowered.common.bridge.world.level.chunk.LevelChunkBridge;
import org.spongepowered.common.bridge.world.level.chunk.TrackedLevelChunkBridge;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhasePrinter;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.context.transaction.ChangeBlock;
import org.spongepowered.common.event.tracking.context.transaction.pipeline.ChunkPipeline;
import org.spongepowered.common.event.tracking.phase.generation.ChunkLoadContext;
import org.spongepowered.common.event.tracking.phase.generation.GenerationPhase;
import org.spongepowered.common.util.PrettyPrinter;
import org.spongepowered.common.world.BlockChange;
import org.spongepowered.common.world.SpongeBlockChangeFlag;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;


@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin_Tracker implements TrackedLevelChunkBridge {
    // @formatter:off
    @Shadow @Final public static LevelChunkSection EMPTY_SECTION;
    @Shadow @Final private LevelChunkSection[] sections;
    @Shadow @Final private net.minecraft.world.level.Level level;

    @Shadow public abstract @Nullable BlockEntity shadow$getBlockEntity(BlockPos pos, LevelChunk.EntityCreationType creationMode);
    @Shadow public abstract BlockState getBlockState(BlockPos pos);
    // @formatter:on
    private @MonotonicNonNull PhaseContext<@NonNull ?> tracker$postProcessContext = null;

    @Inject(method = "setBlockState", at = @At("HEAD"), cancellable = true)
    private void tracker$sanityCheckServerWorldSetBlockState(final BlockPos pos, final BlockState state, final boolean isMoving,
        final CallbackInfoReturnable<BlockState> cir
    ) {
        if (!((WorldBridge) this.level).bridge$isFake()) {
            new PrettyPrinter(80).add("Illegal Direct Chunk Access")
                .hr()
                .add(new IllegalAccessException("No one should be accessing Chunk.setBlock in a ServerWorld's environment"))
                .log(PhaseTracker.LOGGER, Level.WARN);
            cir.setReturnValue(null);
        }
    }

    /**
     * Technically a full overwrite for {@link LevelChunk#setBlockState(BlockPos, BlockState, boolean)}
     * and due to Sponge's hijacking of {@link ServerLevel#setBlock(BlockPos, BlockState, int)},
     * it needs to be able to record transactions when necessary. This implementation allows for us to
     * further specify the types of transactions and what proxies are needing to set up where.
     *
     * @param pos The position changing
     * @param newState The new state
     * @param currentState The current state - passed in from either chunk or world
     * @param flag The sponge change flag, converted from an int to a proper struct
     * @return The changed block state if not null
     * @author gabizou - January 13th, 2020 - Minecraft 1.14.3
     */
    @Override
    @NonNull
    public ChunkPipeline bridge$createChunkPipeline(final BlockPos pos, final BlockState newState, final BlockState currentState,
            final SpongeBlockChangeFlag flag, final int limit) {
        final boolean isFake = ((WorldBridge) this.level).bridge$isFake();
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
        LevelChunkSection chunksection = this.sections[yPos >> 4];
        if (chunksection == LevelChunkMixin_Tracker.EMPTY_SECTION) {
            if (newState.isAir()) {
                return ChunkPipeline.nullReturn((LevelChunk) (Object) this, (ServerLevel) this.level);
            }

            chunksection = new LevelChunkSection(yPos >> 4 << 4);
            this.sections[yPos >> 4] = chunksection;
        }

        // Sponge Start - Build out the BlockTransaction
        final PhaseContext<@NonNull ?> context = PhaseTracker.getInstance().getPhaseContext();
        final @Nullable BlockEntity existing = this.shadow$getBlockEntity(pos, LevelChunk.EntityCreationType.CHECK);
        // Build a transaction maybe?
        final WeakReference<ServerLevel> ref = new WeakReference<>((ServerLevel) this.level);
        final SpongeBlockSnapshot snapshot = TrackingUtil.createPooledSnapshot(currentState, pos, flag, limit, existing,
            () -> Objects.requireNonNull(ref.get(), "ServerWorld dereferenced"),
            Optional::empty, Optional::empty
        );

        // Pulled up from below
        final ChangeBlock transaction = context.createTransaction(snapshot, newState, flag);

        snapshot.blockChange = context.associateBlockChangeWithSnapshot(
            newState,
            currentState
        );
        if (((BlockStateBridge) snapshot.state()).bridge$hasTileEntity()
            && (snapshot.blockChange == BlockChange.BREAK || snapshot.blockChange == BlockChange.MODIFY)) {
            transaction.queuedRemoval = existing;
        }

        final ChunkPipeline.Builder builder = ChunkPipeline.builder()
            .kickOff(transaction)
            .chunk((LevelChunk) (Object) this)
            .chunkSection(chunksection)
            .world((ServerLevel) this.level);

        // Populate the effects
        transaction.populateChunkEffects(builder);

        return builder.build();
    }

    @Inject(method = "addEntity", at = @At("RETURN"))
    private void tracker$SetActiveChunkOnEntityAdd(final Entity entityIn, final CallbackInfo ci) {
        ((ActiveChunkReferantBridge) entityIn).bridge$setActiveChunk(this);
    }

    @Inject(method = "removeEntity(Lnet/minecraft/world/entity/Entity;I)V", at = @At("RETURN"))
    private void tracker$ResetEntityActiveChunk(final Entity entityIn, final int index, final CallbackInfo ci) {
        ((ActiveChunkReferantBridge) entityIn).bridge$setActiveChunk(null);
    }

    @Inject(method = "postProcessGeneration", at = @At("HEAD"))
    private void tracker$startChunkPostProcess(final CallbackInfo ci) {
        if (this.tracker$postProcessContext != null) {
            PhasePrinter.printMessageWithCaughtException(PhaseTracker.SERVER, "Expected to not have a chunk post process", "Chunk Post Process has not completed!", GenerationPhase.State.CHUNK_LOADING, this.tracker$postProcessContext, new NullPointerException("spongecommon.ChunkMixin_Tracker:tracker$postProcessContext is Null"));
            this.tracker$postProcessContext.close();
        }
        this.tracker$postProcessContext = GenerationPhase.State.CHUNK_LOADING.createPhaseContext(PhaseTracker.SERVER)
            .chunk((LevelChunk) (Object) this)
            .world((ServerLevel) this.level)
            .buildAndSwitch();
    }

    @Inject(method = "postProcessGeneration", at = @At("RETURN"))
    private void tracker$endChunkPostProcess(final CallbackInfo ci) {
        if (this.tracker$postProcessContext == null) {
            PhasePrinter.printMessageWithCaughtException(PhaseTracker.getInstance(), "Expected to complete Chunk Post Process", "Chunk Post Process has a null PhaseContext", new NullPointerException("spongecommon.ChunkMixin_Tracker:tracker$postProcessContext is Null"));
        }
        this.tracker$postProcessContext.close();
        this.tracker$postProcessContext = null;
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

    @Inject(
        method = "setBlockEntity(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;clearRemoved()V"))
    private void tracker$SetActiveChunkOnTileEntityAdd(final BlockPos pos, final BlockEntity tileEntityIn, final CallbackInfo ci) {
        ((ActiveChunkReferantBridge) tileEntityIn).bridge$setActiveChunk(this);
        // Make sure to set creator/notifier for TE if any chunk data exists
        // Failure to do this during chunk load will cause TE's to not have proper user tracking
        ((CreatorTrackedBridge) tileEntityIn).tracked$setTrackedUUID(PlayerTracker.Type.CREATOR, ((LevelChunkBridge) this).bridge$getBlockCreatorUUID(pos).orElse(null));
        ((CreatorTrackedBridge) tileEntityIn).tracked$setTrackedUUID(PlayerTracker.Type.NOTIFIER, null);
    }

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @Inject(method = {
        "getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/List;Ljava/util/function/Predicate;)V",
        "getEntities(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/phys/AABB;Ljava/util/List;Ljava/util/function/Predicate;)V",
        "getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;Ljava/util/List;Ljava/util/function/Predicate;)V",
    }, at = @At("RETURN"))
    private void tracker$ThrowCollisionEvent(final @Coerce Object entityIn, final AABB aabb, final List<Entity> listToFill,
        final Predicate<?> filter, final CallbackInfo ci
    ) {
        if (((WorldBridge) this.level).bridge$isFake() || !PhaseTracker.getInstance().getPhaseContext().allowsEntityCollisionEvents()) {
            return;
        }

        if (listToFill.isEmpty()) {
            return;
        }

        if (!ShouldFire.COLLIDE_ENTITY_EVENT) {
            return;
        }

        final @Nullable Entity entity = entityIn instanceof Entity ? ((Entity) entityIn) : null;
        if (SpongeCommonEventFactory.callCollideEntityEvent(entity, listToFill).isCancelled()) {
            listToFill.clear();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Redirect(method = "unpackTicks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ProtoTickList;copyOut(Lnet/minecraft/world/level/TickList;Ljava/util/function/Function;)V"))
    private void tracker$wrapRescheduledTicks(final ProtoTickList chunkPrimerTickList, final TickList<?> tickList, final Function<BlockPos, ?> func) {
        if (!PhaseTracker.SERVER.onSidedThread()) {
            return;
        }
        try (final ChunkLoadContext context = GenerationPhase.State.CHUNK_LOADING.createPhaseContext(PhaseTracker.SERVER)) {
            context.chunk((LevelChunk) (Object) this);
            context.buildAndSwitch();
            chunkPrimerTickList.copyOut(tickList, func);
        }
    }
}
