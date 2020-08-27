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
package org.spongepowered.common.mixin.tracker.world.server;

import co.aikar.timings.Timing;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.Level;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.bridge.TimingBridge;
import org.spongepowered.common.bridge.TrackableBridge;
import org.spongepowered.common.bridge.block.TrackedBlockBridge;
import org.spongepowered.common.bridge.world.TrackedWorldBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.bridge.world.chunk.TrackedChunkBridge;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.BlockChangeFlagManager;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhasePrinter;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.ScheduledBlockChange;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.context.transaction.GameTransaction;
import org.spongepowered.common.event.tracking.context.transaction.effect.AddTileEntityToLoadedListInWorldEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.AddTileEntityToTickableListEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.AddTileEntityToWorldWhileProcessingEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.CheckBlockPostPlacementIsSameEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.EffectResult;
import org.spongepowered.common.event.tracking.context.transaction.effect.NotifyClientEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.NotifyNeighborSideEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.PipelineCursor;
import org.spongepowered.common.event.tracking.context.transaction.effect.RemoveProposedTileEntitiesDuringSetIfWorldProcessingEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.RemoveTileEntityFromChunkEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.RemoveTileEntityFromWorldEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.ReplaceTileEntityInWorldEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.TileOnLoadDuringAddToWorldEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.UpdateConnectingBlocksEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.UpdateLightSideEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.UpdateWorldRendererEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.WorldBlockChangeCompleteEffect;
import org.spongepowered.common.event.tracking.context.transaction.pipeline.ChunkPipeline;
import org.spongepowered.common.event.tracking.context.transaction.pipeline.TileEntityPipeline;
import org.spongepowered.common.event.tracking.context.transaction.pipeline.WorldPipeline;
import org.spongepowered.common.mixin.tracker.world.WorldMixin_Tracker;
import org.spongepowered.common.util.PrettyPrinter;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.SpongeBlockChangeFlag;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin_Tracker extends WorldMixin_Tracker implements TrackedWorldBridge {

    @Shadow @Final private MinecraftServer server;
    private final LinkedBlockingDeque<ScheduledBlockChange> tracker$scheduledChanges = new LinkedBlockingDeque<>();


    @Inject(method = "onEntityAdded", at = @At("TAIL"))
    private void tracker$setEntityTrackedInWorld(final net.minecraft.entity.Entity entityIn, final CallbackInfo ci) {
        if (!this.bridge$isFake()) { // Only set the value if the entity is not fake
            ((TrackableBridge) entityIn).bridge$setWorldTracked(true);
        }
    }

    @Inject(method = "onEntityRemoved", at = @At("TAIL"))
    private void tracker$setEntityUntrackedInWorld(final net.minecraft.entity.Entity entityIn, final CallbackInfo ci) {
        if (!this.bridge$isFake() || ((TrackableBridge) entityIn).bridge$isWorldTracked()) {
            ((TrackableBridge) entityIn).bridge$setWorldTracked(false);
        }
    }

    /**
     * We want to redirect the consumer caller ("updateEntity") because we need to wrap around
     * global entities being ticked, and then entities themselves being ticked is a different area/section.
     *
     * @param serverWorld
     * @param consumer
     * @param entity
     */
    @Redirect(method = "tick",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/server/ServerWorld;guardEntityTick(Ljava/util/function/Consumer;Lnet/minecraft/entity/Entity;)V"),
        slice = @Slice(
            from = @At(value = "INVOKE",
                target = "Lnet/minecraft/world/server/ServerWorld;resetUpdateEntityTick()V"),
            to = @At(value = "INVOKE",
                target = "Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;int2ObjectEntrySet()Lit/unimi/dsi/fastutil/objects/ObjectSet;",
                remap = false
            )
        )
    )
    private void tracker$wrapGlobalEntityTicking(final ServerWorld serverWorld, final Consumer<Entity> consumer, final Entity entity) {
        final PhaseContext<?> currentContext = PhaseTracker.SERVER.getPhaseContext();
        if (currentContext.state.alreadyCapturingEntityTicks()) {
            this.shadow$guardEntityTick(consumer, entity);
            return;
        }
        TrackingUtil.tickGlobalEntity(consumer, entity);
        // TODO - determine if updating rotation is needed.
    }

    @Redirect(method = "tick",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/server/ServerWorld;guardEntityTick(Ljava/util/function/Consumer;Lnet/minecraft/entity/Entity;)V"),
        slice = @Slice(
            from = @At(value = "INVOKE_STRING",
                target = "Lnet/minecraft/profiler/IProfiler;startSection(Ljava/lang/String;)V",
                args = "ldc=tick"),
            to = @At(value = "INVOKE_STRING",
                target = "Lnet/minecraft/profiler/IProfiler;startSection(Ljava/lang/String;)V",
                args = "ldc=remove")
        )
    )
    private void tracker$wrapNormalEntityTick(final ServerWorld serverWorld, final Consumer<Entity> entityUpdateConsumer,
        final Entity entity
    ) {
        final IPhaseState<?> currentState = PhaseTracker.SERVER.getCurrentState();
        if (currentState.alreadyCapturingEntityTicks()) {
            this.shadow$guardEntityTick(entityUpdateConsumer, entity);
            return;
        }
        TrackingUtil.tickEntity(entityUpdateConsumer, entity);
    }

    @Override
    protected void tracker$wrapTileEntityTick(final ITickableTileEntity tileEntity) {
        if (!SpongeImplHooks.shouldTickTile(tileEntity)) {
            return;
        }
        final IPhaseState<?> state = PhaseTracker.SERVER.getCurrentState();
        if (state.alreadyCapturingTileTicks()) {
            tileEntity.tick();
            return;
        }
        TrackingUtil.tickTileEntity(this, tileEntity);
    }


    /**
     * For PhaseTracking, we need to wrap around the
     * {@link BlockState#tick(World, BlockPos, Random)} method, and the ScheduledTickList uses a lambda method
     * to {@code ServerWorld#tickBlock(NextTickListEntry)}, so it's either we customize the ScheduledTickList
     * or we wrap in this method here.
     *
     * @param blockState The block state being ticked
     * @param worldIn The world (this world)
     * @param pos The position of the block state
     * @param random The random (world.rand)
     * @author gabizou - January 11th, 2020 - Minecraft 1.14.3
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Redirect(method = "tickBlock",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/block/BlockState;tick(Lnet/minecraft/world/server/ServerWorld;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V"))
    private void tracker$wrapBlockTick(BlockState blockState, ServerWorld worldIn, BlockPos posIn, Random randomIn) {
        final PhaseContext<?> currentContext = PhaseTracker.SERVER.getPhaseContext();
        final IPhaseState currentState = currentContext.state;
        if (currentState.alreadyCapturingBlockTicks(currentContext) || currentState.ignoresBlockUpdateTick(currentContext)) {
            blockState.tick(worldIn, posIn, randomIn);
            return;
        }
        TrackingUtil.updateTickBlock(this, blockState, posIn, randomIn);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Redirect(method = "tickFluid",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/fluid/IFluidState;tick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"))
    private void tracker$wrapFluidTick(final IFluidState fluidState, final World worldIn, final BlockPos pos) {
        final PhaseContext<?> currentContext = PhaseTracker.SERVER.getPhaseContext();
        final IPhaseState currentState = currentContext.state;
        if (currentState.alreadyCapturingBlockTicks(currentContext) || currentState.ignoresBlockUpdateTick(currentContext)) {
            fluidState.tick(worldIn, pos);
            return;
        }
        TrackingUtil.updateTickFluid(this, fluidState, pos);
    }

    /**
     * For PhaseTracking, we need to wrap around the
     * {@link BlockState#tick(World, BlockPos, Random)} method, and the ScheduledTickList uses a lambda method
     * to {@code ServerWorld#tickBlock(NextTickListEntry)}, so it's either we customize the ScheduledTickList
     * or we wrap in this method here.
     *
     * @author gabizou - January 11th, 2020 - Minecraft 1.14.3
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Redirect(method = "tickEnvironment",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/block/BlockState;randomTick(Lnet/minecraft/world/server/ServerWorld;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V"))
    private void tracker$wrapBlockRandomTick(BlockState blockState, ServerWorld worldIn, BlockPos posIn, Random randomIn) {
        try (final Timing timing = ((TimingBridge) blockState.getBlock()).bridge$getTimingsHandler()) {
            timing.startTiming();
            final PhaseContext<?> context = PhaseTracker.getInstance().getPhaseContext();
            final IPhaseState phaseState = context.state;
            if (phaseState.alreadyCapturingBlockTicks(context)) {
                blockState.randomTick(worldIn, posIn, this.rand);
            } else {
                TrackingUtil.randomTickBlock(this, blockState, posIn, this.rand);
            }
        }
    }

    /**
     * @author gabizou, March 12th, 2016
     * <p>
     * Move this into WorldServer as we should not be modifying the client world.
     * <p>
     * Purpose: Rewritten to support capturing blocks
     */
    @Override
    public boolean setBlockState(final BlockPos pos, final net.minecraft.block.BlockState newState, final int flags) {
        if (World.isOutsideBuildHeight(pos)) {
            return false;
        } else if (this.worldInfo.getGenerator() == WorldType.DEBUG_ALL_BLOCK_STATES) { // isRemote is always false since this is WorldServer
            return false;
        }
        // Sponge Start - Sanity check against the PhaseTracker for instances
        if (this.bridge$isFake()) {
            return super.setBlockState(pos, newState, flags);
        }
        final PhaseTracker instance = PhaseTracker.getInstance();
        if (instance.getSidedThread() != PhaseTracker.SERVER.getSidedThread() && instance != PhaseTracker.SERVER) {
            throw new UnsupportedOperationException("Cannot perform a tracked Block Change on a ServerWorld while not on the main thread!");
        }
        final SpongeBlockChangeFlag spongeFlag = BlockChangeFlagManager.fromNativeInt(flags);

        final Chunk chunk = this.shadow$getChunkAt(pos);
        if (chunk.isEmpty()) {
            return false;
        }

        final net.minecraft.block.BlockState currentState = chunk.getBlockState(pos);

        final TrackedChunkBridge mixinChunk = (TrackedChunkBridge) chunk;

        // Then build and use the BlockPipeline
        final ChunkPipeline chunkPipeline = mixinChunk.bridge$createChunkPipeline(pos, newState, currentState, spongeFlag);
        final WorldPipeline.Builder worldPipelineBuilder = WorldPipeline.builder(chunkPipeline);
        worldPipelineBuilder.addEffect((pipeline, oldState, newState1, flag1) -> {
            if (oldState == null) {
                return EffectResult.NULL_RETURN;
            }
            return EffectResult.NULL_PASS;
        })
            .addEffect(new UpdateLightSideEffect())
            .addEffect(CheckBlockPostPlacementIsSameEffect.getInstance())
            .addEffect(new UpdateWorldRendererEffect())
            .addEffect(new NotifyClientEffect())
            .addEffect(new NotifyNeighborSideEffect())
            .addEffect(new UpdateConnectingBlocksEffect())
            .addEffect(new WorldBlockChangeCompleteEffect());
        final WorldPipeline pipeline = worldPipelineBuilder
            .build();

        return pipeline.processEffects(instance.getPhaseContext(), currentState, newState, pos, spongeFlag);
    }

    @Override
    public SpongeBlockSnapshot bridge$createSnapshot(final net.minecraft.block.BlockState state, final BlockPos pos,
        final BlockChangeFlag updateFlag
    ) {
        final SpongeBlockSnapshotBuilder builder = SpongeBlockSnapshotBuilder.pooled();
        builder.reset();
        builder.blockState(state)
            .world(((WorldProperties) this.worldInfo).getKey())
            .position(VecHelper.toVector3i(pos));
        final Chunk chunk = this.shadow$getChunkAt(pos);
        if (chunk == null) {
            return builder.flag(updateFlag).build();
        }
        final Optional<UUID> creator = ((ChunkBridge) chunk).bridge$getBlockCreatorUUID(pos);
        final Optional<UUID> notifier = ((ChunkBridge) chunk).bridge$getBlockNotifierUUID(pos);
        creator.ifPresent(builder::creator);
        notifier.ifPresent(builder::notifier);
        final boolean hasTileEntity = SpongeImplHooks.hasBlockTileEntity(state);
        final TileEntity tileEntity = chunk.getTileEntity(pos, Chunk.CreateEntityType.CHECK);
        if (hasTileEntity || tileEntity != null) {
            // We MUST only check to see if a TE exists to avoid creating a new one.
            if (tileEntity != null) {
                // TODO - custom data.
                final CompoundNBT nbt = new CompoundNBT();
                // Some mods like OpenComputers assert if attempting to save robot while moving
                try {
                    tileEntity.write(nbt);
                    builder.unsafeNbt(nbt);
                } catch (final Throwable t) {
                    // ignore
                }
            }
        }
        builder.flag(updateFlag);
        return builder.build();
    }

    /**
     * Technically an overwrite, but because this is simply an override, we can
     * effectively do as we need to, which is determine if we are performing
     * block transactional recording, go ahead and record transactions.
     * <p>In the event that we are performing transaction recording, the following
     * must be true:
     * <ul>
     *     <li>This world instance is managed and verified by Sponge</li>
     *     <li>This world must {@link WorldBridge#bridge$isFake()} return {@code false}</li>
     *     <li>The {@link PhaseTracker#SERVER}'s {@link PhaseTracker#getSidedThread()} must be {@code ==} {@link Thread#currentThread()}</li
     *     <li>The current {@link IPhaseState} must be allowing to record transactions with an applicable {@link org.spongepowered.common.event.tracking.context.transaction.TransactionalCaptureSupplier}</li>
     * </ul>
     * After which, we may be able to appropriately associate the {@link TileEntity}
     * being removed with either an existing {@link GameTransaction.ChangeBlock},
     * or generate a new {@link GameTransaction.RemoveTileEntity} transaction
     * that would otherwise be able to associate with either the current {@link IPhaseState} or a parent {@link GameTransaction}
     * if this call is the result of a {@link org.spongepowered.common.event.tracking.context.transaction.effect.ProcessingSideEffect}..
     *
     * @param pos The position of the tile entity to remove
     * @author gabizou - July 31st, 2020 - Minecraft 1.14.3
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void shadow$removeTileEntity(final BlockPos pos) {
        final BlockPos immutable = pos.toImmutable();
        final TileEntity tileentity = this.shadow$getTileEntity(immutable);
        if (tileentity == null) {
            return;
        }
        if (this.bridge$isFake() || PhaseTracker.SERVER.getSidedThread() != Thread.currentThread()) {
            // If we're fake or not on the server thread, well, we could effectively call
            // out whoever is trying to remove tile entities asynchronously....
            super.shadow$removeTileEntity(immutable);
            return;
        }
        // Otherwise, let's go on and check if we're recording transactions,
        // and if so, log the tile entity removal (may associate with an existing transaction,
        // or create a new transaction.
        final PhaseContext<?> current = PhaseTracker.SERVER.getPhaseContext();
        final IPhaseState state = current.state;
        if (state.doesBulkBlockCapture(current)) {
            if (current.getBlockTransactor().logTileRemoval(tileentity, () -> (ServerWorld) (Object) this)) {
                final TileEntityPipeline pipeline = TileEntityPipeline.kickOff((ServerWorld) (Object) this, immutable)
                    .addEffect(new RemoveTileEntityFromWorldEffect())
                    .addEffect(new RemoveTileEntityFromChunkEffect())
                    .build();
                pipeline.processEffects(current, new PipelineCursor(tileentity.getBlockState(), 0,immutable, tileentity));
                return;
            }
        }
        super.shadow$removeTileEntity(immutable);
    }

    @SuppressWarnings({"rawtypes", "unchecked", "ConstantConditions", "RedundantCast"})
    @Override
    public boolean shadow$addTileEntity(final TileEntity tileEntity) {
        if (this.bridge$isFake() || PhaseTracker.SERVER.getSidedThread() != Thread.currentThread()) {
            // If we're fake or not on the server thread, well, we could effectively call
            // out whoever is trying to remove tile entities asynchronously....
            return super.shadow$addTileEntity(tileEntity);
        }
        // Otherwise, let's go on and check if we're recording transactions,
        // and if so, log the tile entity removal (may associate with an existing transaction,
        // or create a new transaction.
        final PhaseContext<?> current = PhaseTracker.SERVER.getPhaseContext();
        final IPhaseState state = current.state;
        if (state.doesBulkBlockCapture(current)) {
            final BlockPos immutable = tileEntity.getPos().toImmutable();
            if (tileEntity.getWorld() != (ServerWorld) (Object) this) {
                tileEntity.setWorldAndPos((ServerWorld) (Object) this, immutable);
            }
            final Chunk chunk = this.shadow$getChunkAt(immutable);
            if (current.getBlockTransactor().logTileAddition(tileEntity, () -> (ServerWorld) (Object) this, chunk)) {
                final TileEntityPipeline pipeline = TileEntityPipeline.kickOff((ServerWorld) (Object) this, immutable)
                    .addEffect(new AddTileEntityToWorldWhileProcessingEffect())
                    .addEffect(new AddTileEntityToLoadedListInWorldEffect())
                    .addEffect(new AddTileEntityToTickableListEffect())
                    .addEffect(new TileOnLoadDuringAddToWorldEffect())
                    .build();
                return pipeline.processEffects(current, new PipelineCursor(tileEntity.getBlockState(), 0,immutable, tileEntity));
            }
        }

        return super.shadow$addTileEntity(tileEntity);
    }

    @SuppressWarnings({"RedundantCast", "ConstantConditions", "unchecked", "rawtypes"})
    @Override
    public void shadow$setTileEntity(final BlockPos pos, @Nullable final TileEntity proposed) {
        final BlockPos immutable = pos.toImmutable();
        if (this.bridge$isFake() || PhaseTracker.SERVER.getSidedThread() != Thread.currentThread()) {
            // If we're fake or not on the server thread, well, we could effectively call
            // out whoever is trying to remove tile entities asynchronously....
            super.shadow$setTileEntity(pos, proposed);
            return;
        }
        if (proposed != null) {
            if (proposed.getWorld() != (ServerWorld) (Object) this) {
                proposed.setWorldAndPos((ServerWorld) (Object) this, immutable);
            } else {
                proposed.setPos(pos);
            }
        }
        // Otherwise, let's go on and check if we're recording transactions,
        // and if so, log the tile entity removal (may associate with an existing transaction,
        // or create a new transaction.
        final PhaseContext<?> current = PhaseTracker.SERVER.getPhaseContext();
        final IPhaseState state = current.state;
        if (state.doesBulkBlockCapture(current)) {
            final @Nullable TileEntity existing = this.shadow$getChunkAt(immutable).getTileEntity(immutable);
            if (current.getBlockTransactor().logTileReplacement(immutable, existing, proposed, () -> (ServerWorld) (Object) this)) {
                final TileEntityPipeline pipeline = TileEntityPipeline.kickOff((ServerWorld) (Object) this, immutable)
                    .addEffect(new RemoveProposedTileEntitiesDuringSetIfWorldProcessingEffect())
                    .addEffect(new ReplaceTileEntityInWorldEffect())
                    .build();
                pipeline.processEffects(current, new PipelineCursor(proposed.getBlockState(), 0,immutable, proposed));
                return;
            }
        }

        super.shadow$setTileEntity(immutable, proposed);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void shadow$neighborChanged(final BlockPos pos, final Block blockIn, final BlockPos fromPos) {
        final BlockPos immutableTarget = pos.toImmutable();
        final BlockPos immutableFrom = fromPos.toImmutable();
        // Sponge Start - Check asynchronicity,
        // if not on the server thread and we're a server world, we've got problems...
        final PhaseTracker server = PhaseTracker.SERVER;
        if (server.getSidedThread() != Thread.currentThread()) {
            // lol no, report the block change properly
            new PrettyPrinter(60).add("Illegal Async PhaseTracker Access").centre().hr()
                .addWrapped(PhasePrinter.ASYNC_TRACKER_ACCESS)
                .add()
                // TODO - have the PhaseTracker of this particular thread print its stack
                // since we're on that thread, maybe we might have some idea of who or what is calling it.
                .add(new Exception("Async Block Notifcation Detected"))
                .log(SpongeCommon.getLogger(), Level.ERROR);
            // Maybe? I don't think this is wise to try and sync back a notification on the main thread.
            return;
        }
        // But, sometimes we need to say that we're on the right thread, but it's a silly mod's specific
        // world that Sponge isn't directly managing, so we'll just ignore trying to record on those.
        if (this.bridge$isFake()) {
            // If we're fake, well, we could effectively call this without recording on worlds we don't
            // want to care about.
            super.shadow$neighborChanged(immutableTarget, blockIn, immutableFrom);
            return;
        }
        // Otherwise, we continue with recording, maybe.
        final Chunk targetChunk = this.shadow$getChunkAt(immutableTarget);
        final BlockState targetBlockState = targetChunk.getBlockState(immutableTarget);
        // Sponge - Shortcircuit if the block has no neighbor logic
        if (!((TrackedBlockBridge) targetBlockState.getBlock()).bridge$overridesNeighborNotificationLogic()) {
            return;
        }
        // Sponge End

        // Sponge start - prepare notification
        final PhaseContext<?> peek = server.getPhaseContext();
        final IPhaseState state = peek.state;

        //  try { // Vanilla - We need to push the effect transactor so that it always pops
        try {
            final WeakReference<ServerWorld> worldReference = new WeakReference<>((ServerWorld) (Object) this);
            final Supplier<ServerWorld> worldSupplier = () -> Objects.requireNonNull(worldReference.get(), "ServerWorld dereferenced");
            final @Nullable TileEntity existingTile = targetChunk.getTileEntity(immutableTarget, Chunk.CreateEntityType.CHECK);
            peek.getBlockTransactor().logNeighborNotification(worldSupplier, immutableFrom, blockIn, immutableTarget, targetBlockState, existingTile);

            state.associateNeighborStateNotifier(peek, immutableFrom, targetBlockState.getBlock(), immutableTarget, ((ServerWorld) (Object) this), PlayerTracker.Type.NOTIFIER);
            // Sponge End

            targetBlockState.neighborChanged(((ServerWorld) (Object) this), immutableTarget, blockIn, immutableFrom, false);

        } catch (final Throwable throwable) {
            final CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception while updating neighbours");
            final CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being updated");
            crashreportcategory.addDetail("Source block type", () -> {
                try {
                    return String.format("ID #%d (%s // %s)", Registry.BLOCK.getId(blockIn),
                        blockIn.getTranslationKey(), blockIn.getClass().getCanonicalName());
                } catch (final Throwable var2) {
                    return "ID #" + Registry.BLOCK.getId(blockIn);
                }
            });
            CrashReportCategory.addBlockInfo(crashreportcategory, immutableTarget, targetBlockState);
            throw new ReportedException(crashreport);
        }


    }
}
