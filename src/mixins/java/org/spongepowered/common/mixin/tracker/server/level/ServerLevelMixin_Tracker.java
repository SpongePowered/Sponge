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
package org.spongepowered.common.mixin.tracker.server.level;

import co.aikar.timings.Timing;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockEventData;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.TickNextTickData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.Level;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.bridge.TimingBridge;
import org.spongepowered.common.bridge.TrackableBridge;
import org.spongepowered.common.bridge.block.BlockBridge;
import org.spongepowered.common.bridge.server.level.ServerLevelBridge;
import org.spongepowered.common.bridge.world.TickNextTickDataBridge;
import org.spongepowered.common.bridge.world.TrackedWorldBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.level.TrackerBlockEventDataBridge;
import org.spongepowered.common.bridge.world.level.block.TrackedBlockBridge;
import org.spongepowered.common.bridge.world.level.block.state.BlockStateBridge;
import org.spongepowered.common.bridge.world.level.chunk.LevelChunkBridge;
import org.spongepowered.common.bridge.world.level.chunk.TrackedLevelChunkBridge;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.BlockChangeFlagManager;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhasePrinter;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.context.transaction.ChangeBlock;
import org.spongepowered.common.event.tracking.context.transaction.GameTransaction;
import org.spongepowered.common.event.tracking.context.transaction.RemoveTileEntity;
import org.spongepowered.common.event.tracking.context.transaction.effect.AddTileEntityToLoadedListInWorldEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.AddTileEntityToTickableListEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.AddTileEntityToWorldWhileProcessingEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.CheckBlockPostPlacementIsSameEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.EffectResult;
import org.spongepowered.common.event.tracking.context.transaction.effect.NotifyClientEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.NotifyNeighborSideEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.PerformBlockDropsFromDestruction;
import org.spongepowered.common.event.tracking.context.transaction.effect.RemoveProposedTileEntitiesDuringSetIfWorldProcessingEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.RemoveTileEntityFromChunkEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.RemoveTileEntityFromWorldEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.ReplaceTileEntityInWorldEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.TileOnLoadDuringAddToWorldEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.UpdateConnectingBlocksEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.UpdateLightSideEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.UpdateWorldRendererEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.WorldBlockChangeCompleteEffect;
import org.spongepowered.common.event.tracking.context.transaction.effect.WorldDestroyBlockLevelEffect;
import org.spongepowered.common.event.tracking.context.transaction.pipeline.ChunkPipeline;
import org.spongepowered.common.event.tracking.context.transaction.pipeline.PipelineCursor;
import org.spongepowered.common.event.tracking.context.transaction.pipeline.TileEntityPipeline;
import org.spongepowered.common.event.tracking.context.transaction.pipeline.WorldPipeline;
import org.spongepowered.common.event.tracking.phase.generation.GenerationPhase;
import org.spongepowered.common.mixin.tracker.world.level.LevelMixin_Tracker;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.PrettyPrinter;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.SpongeBlockChangeFlag;
import org.spongepowered.common.world.server.SpongeLocatableBlockBuilder;
import org.spongepowered.common.world.volume.VolumeStreamUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin_Tracker extends LevelMixin_Tracker implements TrackedWorldBridge {

    // @formatting:off
    @Shadow @Final private List<ServerPlayer> players;
    // @formatting:on


    @Inject(method = "add", at = @At("TAIL"))
    private void tracker$setEntityTrackedInWorld(final net.minecraft.world.entity.Entity entityIn, final CallbackInfo ci) {
        if (!this.bridge$isFake()) { // Only set the value if the entity is not fake
            ((TrackableBridge) entityIn).bridge$setWorldTracked(true);
        }
    }

    @Inject(method = "onEntityRemoved", at = @At("TAIL"))
    private void tracker$setEntityUntrackedInWorld(final net.minecraft.world.entity.Entity entityIn, final CallbackInfo ci) {
        if (!this.bridge$isFake() || ((TrackableBridge) entityIn).bridge$isWorldTracked()) {
            ((TrackableBridge) entityIn).bridge$setWorldTracked(false);
        }
    }


    @Redirect(method = "tick",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;guardEntityTick(Ljava/util/function/Consumer;Lnet/minecraft/world/entity/Entity;)V"),
        slice = @Slice(
            from = @At(value = "INVOKE_STRING",
                target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V",
                args = "ldc=tick"),
            to = @At(value = "INVOKE_STRING",
                target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V",
                args = "ldc=remove")
        )
    )
    private void tracker$wrapNormalEntityTick(final ServerLevel serverWorld, final Consumer<Entity> entityUpdateConsumer,
        final Entity entity
    ) {
        ((ServerLevelBridge) this).bridge$getTimingsHandler().entityTick.startTiming();
        final PhaseContext<@NonNull ?> currentState = PhaseTracker.SERVER.getPhaseContext();
        if (currentState.alreadyCapturingEntityTicks()) {
            this.shadow$guardEntityTick(entityUpdateConsumer, entity);
            return;
        }
        TrackingUtil.tickEntity(entityUpdateConsumer, entity);
        ((ServerLevelBridge) this).bridge$getTimingsHandler().entityTick.stopTiming();
    }

    @Override
    protected void tracker$wrapTileEntityTick(final TickableBlockEntity tileEntity) {
        final PhaseContext<@NonNull ?> state = PhaseTracker.SERVER.getPhaseContext();
        if (state.alreadyCapturingTileTicks()) {
            tileEntity.tick();
            return;
        }
        TrackingUtil.tickTileEntity(this, tileEntity);
    }


    /**
     * For PhaseTracking, we need to wrap around the
     * {@link BlockState#tick(ServerLevel, BlockPos, Random)} method, and the ScheduledTickList uses a lambda method
     * to {@code ServerWorld#tickBlock(NextTickListEntry)}, so it's either we customize the ScheduledTickList
     * or we wrap in this method here.
     *
     * @param blockState The block state being ticked
     * @param worldIn The world (this world)
     * @param posIn The position of the block
     * @param randomIn The world random
     * @author gabizou - January 11th, 2020 - Minecraft 1.14.3
     */
    @Redirect(method = "tickBlock",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Ljava/util/Random;)V"))
    private void tracker$wrapBlockTick(final BlockState blockState, final ServerLevel worldIn, final BlockPos posIn, final Random randomIn, final TickNextTickData<Block> entry) {
        final PhaseContext<@NonNull ?> currentContext = PhaseTracker.SERVER.getPhaseContext();
        if (currentContext.alreadyCapturingBlockTicks() || currentContext.ignoresBlockUpdateTick()) {
            blockState.tick(worldIn, posIn, randomIn);
            return;
        }
        if (((TickNextTickDataBridge) entry).bridge$isPartOfWorldGeneration()) {
            try (final PhaseContext<@NonNull ?> context = GenerationPhase.State.DEFERRED_SCHEDULED_UPDATE.createPhaseContext(PhaseTracker.SERVER)
                .source(this)
                .scheduledUpdate(entry)
            ) {
                context.buildAndSwitch();
                blockState.tick(worldIn, posIn, randomIn);
            }
            return;
        }
        TrackingUtil.updateTickBlock(this, blockState, posIn, randomIn);
    }

    @Redirect(method = "tickLiquid",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/material/FluidState;tick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"))
    private void tracker$wrapFluidTick(final FluidState fluidState, final net.minecraft.world.level.Level worldIn, final BlockPos pos, final TickNextTickData<Fluid> entry) {
        final PhaseContext<@NonNull ?> currentContext = PhaseTracker.SERVER.getPhaseContext();
        if (currentContext.alreadyCapturingBlockTicks() || currentContext.ignoresBlockUpdateTick()) {
            fluidState.tick(worldIn, pos);
            return;
        }
        if (((TickNextTickDataBridge) entry).bridge$isPartOfWorldGeneration()) {
            try (final PhaseContext<@NonNull ?> context = GenerationPhase.State.DEFERRED_SCHEDULED_UPDATE.createPhaseContext(PhaseTracker.SERVER)
                .source(this)
                .scheduledUpdate(entry)
            ) {
                context.buildAndSwitch();
                fluidState.tick(worldIn, pos);
            }
            return;
        }
        TrackingUtil.updateTickFluid(this, fluidState, pos);
    }

    /**
     * For PhaseTracking, we need to wrap around the
     * {@link BlockState#tick(ServerLevel, BlockPos, Random)} method, and the ScheduledTickList uses a lambda method
     * to {@code ServerWorld#tickBlock(NextTickListEntry)}, so it's either we customize the ScheduledTickList
     * or we wrap in this method here.
     *
     * @author gabizou - January 11th, 2020 - Minecraft 1.14.3
     */
    @Redirect(method = "tickChunk",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;randomTick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Ljava/util/Random;)V"))
    private void tracker$wrapBlockRandomTick(final BlockState blockState, final ServerLevel worldIn, final BlockPos posIn, final Random randomIn) {
        try (final Timing timing = ((TimingBridge) blockState.getBlock()).bridge$getTimingsHandler()) {
            timing.startTiming();
            final PhaseContext<@NonNull ?> context = PhaseTracker.getInstance().getPhaseContext();
            if (context.alreadyCapturingBlockTicks()) {
                blockState.randomTick(worldIn, posIn, this.random);
            } else {
                TrackingUtil.randomTickBlock(this, blockState, posIn, this.random);
            }
        }
    }

    @Redirect(method = "tickChunk",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/material/FluidState;randomTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Ljava/util/Random;)V"
        )
    )
    private void tracker$wrapFluidRandomTick(final FluidState fluidState, final net.minecraft.world.level.Level worldIn, final BlockPos pos, final Random random) {
        final PhaseContext<@NonNull ?> context = PhaseTracker.getInstance().getPhaseContext();
        if (context.alreadyCapturingBlockTicks()) {
            fluidState.randomTick(worldIn, pos, this.random);
        } else {
            TrackingUtil.randomTickFluid(this, fluidState, pos, this.random);
        }
    }

    @Redirect(method = "doBlockEvent",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;triggerEvent(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;II)Z"))
    private boolean tracker$wrapBlockStateEventReceived(final BlockState recievingState, final net.minecraft.world.level.Level thisWorld, final BlockPos targetPos, final int eventId, final int flag, final BlockEventData data) {
        return TrackingUtil.fireMinecraftBlockEvent((ServerLevel) (Object) this, data, recievingState);
    }

    @Redirect(
        method = "blockEvent",
        at = @At(
            value = "INVOKE",
            target = "Lit/unimi/dsi/fastutil/objects/ObjectLinkedOpenHashSet;add(Ljava/lang/Object;)Z",
            remap = false
        )
    )
    private boolean tracker$associatePhaseContextDataWithBlockEvent(
        final ObjectLinkedOpenHashSet<BlockEventData> list, final Object data,
        final BlockPos pos, final Block blockIn, final int eventID, final int eventParam
    ) {
        final PhaseContext<@NonNull ?> currentContext = PhaseTracker.getInstance().getPhaseContext();
        final BlockEventData blockEventData = (BlockEventData) data;
        final TrackerBlockEventDataBridge blockEvent = (TrackerBlockEventDataBridge) blockEventData;
        // Short circuit phase states who do not track during block events
        if (currentContext.ignoresBlockEvent()) {
            return list.add(blockEventData);
        }

        final BlockState state = this.shadow$getBlockState(pos);
        if (((BlockBridge) blockIn).bridge$shouldFireBlockEvents()) {
            blockEvent.bridge$setSourceUser(currentContext.getActiveUser());
            if (((BlockStateBridge) state).bridge$hasTileEntity()) {
                blockEvent.bridge$setTileEntity((BlockEntity) this.shadow$getBlockEntity(pos));
            }
            if (blockEvent.bridge$getTileEntity() == null) {
                final LocatableBlock locatable = new SpongeLocatableBlockBuilder()
                    .world((org.spongepowered.api.world.server.ServerWorld) this)
                    .position(pos.getX(), pos.getY(), pos.getZ())
                    .state((org.spongepowered.api.block.BlockState) state)
                    .build();
                blockEvent.bridge$setTickingLocatable(locatable);
            }
        }

        // Short circuit any additional handling. We've associated enough with the BlockEvent to
        // allow tracking to take place for other/future phases
        if (!((BlockBridge) blockIn).bridge$shouldFireBlockEvents()) {
            return list.add((BlockEventData) data);
        }
        // In pursuant with our block updates management, we chose to
        // effectively allow the block event get added to the list, but
        // we log the transaction so that we can call the change block event
        // pre, and if needed, undo the add to the list.
        currentContext.appendNotifierToBlockEvent(this, pos, blockEvent);
        // We fire a Pre event to make sure our captures do not get stuck in a loop.
        // This is very common with pistons as they add block events while blocks are being notified.
        if (ShouldFire.CHANGE_BLOCK_EVENT_PRE) {
            if (blockIn instanceof PistonBaseBlock) {
                // We only fire pre events for pistons
                if (SpongeCommonEventFactory.handlePistonEvent(this, pos, state, eventID)) {
                    return false;
                }
            } else {
                if (SpongeCommonEventFactory.callChangeBlockEventPre((ServerLevelBridge) this, pos).isCancelled()) {
                    return false;
                }
            }
        }
        currentContext.getTransactor().logBlockEvent(state, this, pos, blockEvent);

        return list.add(blockEventData);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Explosion tracker$triggerInternalExplosion(org.spongepowered.api.world.explosion.Explosion explosion,
        final Function<? super Explosion, ? extends PhaseContext<@NonNull ?>> contextCreator) {
        // Sponge start
        final Explosion originalExplosion = (Explosion) explosion;
        if (ShouldFire.EXPLOSION_EVENT_PRE) {
            // Set up the pre event
            final ExplosionEvent.Pre
                    event =
                    SpongeEventFactory.createExplosionEventPre(
                            PhaseTracker.SERVER.currentCause(),
                            explosion, ((org.spongepowered.api.world.server.ServerWorld) this));
            if (SpongeCommon.post(event)) {
                return (Explosion) explosion;
            }
            explosion = event.explosion();
        }
        final Explosion mcExplosion;
        try {
            // Since we already have the API created implementation Explosion, let's use it.
            mcExplosion = (Explosion) explosion;
        } catch (final Exception e) {
            new org.spongepowered.asm.util.PrettyPrinter(60).add("Explosion not compatible with this implementation").centre().hr()
                .add("An explosion that was expected to be used for this implementation does not")
                .add("originate from this implementation.")
                .add(e)
                .trace();
            return originalExplosion;
        }

        try (final PhaseContext<@NonNull ?> ignored = contextCreator.apply(mcExplosion)
            .source(((Optional) explosion.sourceExplosive()).orElse(this))) {
            ignored.buildAndSwitch();
            final boolean damagesTerrain = explosion.shouldBreakBlocks();
            // Sponge End

            mcExplosion.explode();
            mcExplosion.finalizeExplosion(true);

            if (!damagesTerrain) {
                mcExplosion.clearToBlow();
            }

            // Sponge Start - Don't send explosion packets, they're spammy, we can replicate it on the server entirely
            /*
            for (EntityPlayer entityplayer : this.playerEntities) {
                if (entityplayer.getDistanceSq(x, y, z) < 4096.0D) {
                    ((EntityPlayerMP) entityplayer).connection
                        .sendPacket(new SPacketExplosion(x, y, z, strength, mcExplosion.getAffectedBlockPositions(),
                            mcExplosion.getPlayerKnockbackMap().get(entityplayer)));
                }
            }

             */
            // Use the knockback map and set velocities, since the explosion packet isn't sent, we need to replicate
            // the players being moved.
            for (final ServerPlayer playerEntity : this.players) {
                final Vec3 knockback = mcExplosion.getHitPlayers().get(playerEntity);
                if (knockback != null) {
                    // In Vanilla, doExplosionB always updates the 'motion[xyz]' fields for every entity in range.
                    // However, this field is completely ignored for players (since 'velocityChanged') is never set, and
                    // a completely different value is sent through 'SPacketExplosion'.

                    // To replicate this behavior, we manually send a velocity packet. It's critical that we don't simply
                    // add to the 'motion[xyz]' fields, as that will end up using the value set by 'doExplosionB', which must be
                    // ignored.
                    playerEntity.connection.send(new ClientboundSetEntityMotionPacket(playerEntity.getId(), new Vec3(knockback.x, knockback.y, knockback.z)));
                }
            }
            // Sponge End

        }
        // Sponge End
        return mcExplosion;
    }

    @Override
    public Optional<WorldPipeline.Builder> bridge$startBlockChange(final BlockPos pos, final BlockState newState, final int flags) {
        if (net.minecraft.world.level.Level.isOutsideBuildHeight(pos)) {
            return Optional.empty();
        } else if (this.shadow$isDebug()) { // isClientSide is always false since this is WorldServer
            return Optional.empty();
        }
        // Sponge Start - Sanity check against the PhaseTracker for instances
        if (this.bridge$isFake()) {
            return Optional.empty();
        }
        final PhaseTracker instance = PhaseTracker.getInstance();
        if (instance.getSidedThread() != PhaseTracker.SERVER.getSidedThread() && instance != PhaseTracker.SERVER) {
            throw new UnsupportedOperationException("Cannot perform a tracked Block Change on a ServerWorld while not on the main thread!");
        }
        final SpongeBlockChangeFlag spongeFlag = BlockChangeFlagManager.fromNativeInt(flags);

        final LevelChunk chunk = this.shadow$getChunkAt(pos);
        if (chunk.isEmpty()) {
            return Optional.empty();
        }
        final net.minecraft.world.level.block.state.BlockState currentState = chunk.getBlockState(pos);


        return Optional.of(this.bridge$makePipeline(pos, currentState, newState, chunk, spongeFlag, Constants.World.DEFAULT_BLOCK_CHANGE_LIMIT));
    }

    private WorldPipeline.Builder bridge$makePipeline(
        final BlockPos pos,
        final BlockState currentState,
        final BlockState newState,
        final LevelChunk chunk,
        final SpongeBlockChangeFlag spongeFlag,
        final int limit
    ) {
        final TrackedLevelChunkBridge mixinChunk = (TrackedLevelChunkBridge) chunk;

        // Then build and use the BlockPipeline
        final ChunkPipeline chunkPipeline = mixinChunk.bridge$createChunkPipeline(pos, newState, currentState, spongeFlag, limit);
        final WorldPipeline.Builder worldPipelineBuilder = WorldPipeline.builder(chunkPipeline);
        worldPipelineBuilder.addEffect((pipeline, oldState, newState1, flag1, cursorLimit) -> {
            if (oldState == null) {
                return EffectResult.NULL_RETURN;
            }
            return EffectResult.NULL_PASS;
        })
            .addEffect(UpdateLightSideEffect.getInstance())
            .addEffect(CheckBlockPostPlacementIsSameEffect.getInstance())
            .addEffect(UpdateWorldRendererEffect.getInstance())
            .addEffect(NotifyClientEffect.getInstance())
            .addEffect(NotifyNeighborSideEffect.getInstance())
            .addEffect(UpdateConnectingBlocksEffect.getInstance());
        return worldPipelineBuilder;
    }

    /**
     * @author gabizou, March 12th, 2016
     * <p>
     * Move this into WorldServer as we should not be modifying the client world.
     * <p>
     * Purpose: Rewritten to support capturing blocks
     */
    @Override
    public boolean setBlock(final BlockPos pos, final net.minecraft.world.level.block.state.BlockState newState, final int flags, final int limit) {
        if (net.minecraft.world.level.Level.isOutsideBuildHeight(pos)) {
            return false;
        } else if (this.shadow$isDebug()) { // isClientSide is always false since this is WorldServer
            return false;
        }
        // Sponge Start - Sanity check against the PhaseTracker for instances
        if (this.bridge$isFake()) {
            return super.setBlock(pos, newState, flags, limit);
        }
        final PhaseTracker instance = PhaseTracker.getInstance();
        if (instance.getSidedThread() != PhaseTracker.SERVER.getSidedThread() && instance != PhaseTracker.SERVER) {
            throw new UnsupportedOperationException("Cannot perform a tracked Block Change on a ServerWorld while not on the main thread!");
        }
        final SpongeBlockChangeFlag spongeFlag = BlockChangeFlagManager.fromNativeInt(flags);

        final LevelChunk chunk = this.shadow$getChunkAt(pos);
        if (chunk.isEmpty()) {
            return false;
        }
        final net.minecraft.world.level.block.state.BlockState currentState = chunk.getBlockState(pos);
        // Check if the transaction would be rendered redundant, if so, follow minecraft's normal
        // change of "don't do anything if the block is the same".
        if (currentState == newState) {
            return false;
        }
        final WorldPipeline pipeline = this.bridge$makePipeline(pos, currentState, newState, chunk, spongeFlag, limit)
            .addEffect(WorldBlockChangeCompleteEffect.getInstance())
            .build();

        return pipeline.processEffects(instance.getPhaseContext(), currentState, newState, pos, null, spongeFlag, limit);
    }

    @Override
    public boolean destroyBlock(final BlockPos pos, final boolean doDrops, @Nullable final Entity p_241212_3_, final int limit) {
        final BlockState currentState = this.shadow$getBlockState(pos);
        if (currentState.isAir()) {
            return false;
        } else {
            // Sponge Start - Sanity check against the PhaseTracker for instances
            if (this.bridge$isFake()) {
                return super.destroyBlock(pos, doDrops, p_241212_3_, limit);
            }
            final PhaseTracker instance = PhaseTracker.getInstance();
            if (instance.getSidedThread() != PhaseTracker.SERVER.getSidedThread() && instance != PhaseTracker.SERVER) {
                throw new UnsupportedOperationException("Cannot perform a tracked Block Change on a ServerWorld while not on the main thread!");
            }
            final FluidState fluidstate = this.shadow$getFluidState(pos);
            final BlockState emptyBlock = fluidstate.createLegacyBlock();
            final SpongeBlockChangeFlag spongeFlag = BlockChangeFlagManager.fromNativeInt(3);

            final LevelChunk chunk = this.shadow$getChunkAt(pos);
            if (chunk.isEmpty()) {
                return false;
            }
            final WorldPipeline.Builder pipelineBuilder = this.bridge$makePipeline(pos, currentState, emptyBlock, chunk, spongeFlag, limit)
                .addEffect(WorldDestroyBlockLevelEffect.getInstance());

            if (doDrops) {
                pipelineBuilder.addEffect(PerformBlockDropsFromDestruction.getInstance());
            }

            final WorldPipeline pipeline = pipelineBuilder
                .addEffect(WorldBlockChangeCompleteEffect.getInstance())
                .build();

            return pipeline.processEffects(instance.getPhaseContext(), currentState, emptyBlock, pos, p_241212_3_, spongeFlag, limit);
        }
    }

    @Override
    public SpongeBlockSnapshot bridge$createSnapshot(final net.minecraft.world.level.block.state.BlockState state, final BlockPos pos,
        final BlockChangeFlag updateFlag
    ) {
        final SpongeBlockSnapshotBuilder builder = SpongeBlockSnapshotBuilder.pooled();
        builder.reset();
        builder.blockState(state)
            .world((ServerLevel) (Object) this)
            .position(VecHelper.toVector3i(pos));
        final LevelChunk chunk = this.shadow$getChunkAt(pos);
        if (chunk == null) {
            return builder.flag(updateFlag).build();
        }
        final Optional<UUID> creator = ((LevelChunkBridge) chunk).bridge$getBlockCreatorUUID(pos);
        final Optional<UUID> notifier = ((LevelChunkBridge) chunk).bridge$getBlockNotifierUUID(pos);
        creator.ifPresent(builder::creator);
        notifier.ifPresent(builder::notifier);
        final boolean hasTileEntity = ((BlockStateBridge) state).bridge$hasTileEntity();
        final net.minecraft.world.level.block.entity.BlockEntity tileEntity = chunk.getBlockEntity(pos, LevelChunk.EntityCreationType.CHECK);
        if (hasTileEntity || tileEntity != null) {
            // We MUST only check to see if a TE exists to avoid creating a new one.
            if (tileEntity != null) {
                // TODO - custom data.
                final CompoundTag nbt = new CompoundTag();
                // Some mods like OpenComputers assert if attempting to save robot while moving
                try {
                    tileEntity.save(nbt);
                    builder.addUnsafeCompound(nbt);
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
     * After which, we may be able to appropriately associate the {@link net.minecraft.world.level.block.entity.BlockEntity}
     * being removed with either an existing {@link ChangeBlock},
     * or generate a new {@link RemoveTileEntity} transaction
     * that would otherwise be able to associate with either the current {@link IPhaseState} or a parent {@link GameTransaction}
     * if this call is the result of a {@link org.spongepowered.common.event.tracking.context.transaction.effect.ProcessingSideEffect}..
     *
     * @param pos The position of the tile entity to remove
     * @author gabizou - July 31st, 2020 - Minecraft 1.14.3
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void shadow$removeBlockEntity(final BlockPos pos) {
        final BlockPos immutable = pos.immutable();
        final net.minecraft.world.level.block.entity.BlockEntity tileentity = this.shadow$getBlockEntity(immutable);
        if (tileentity == null) {
            return;
        }
        if (this.bridge$isFake() || PhaseTracker.SERVER.getSidedThread() != Thread.currentThread()) {
            // If we're fake or not on the server thread, well, we could effectively call
            // out whoever is trying to remove tile entities asynchronously....
            super.shadow$removeBlockEntity(immutable);
            return;
        }
        // Otherwise, let's go on and check if we're recording transactions,
        // and if so, log the tile entity removal (may associate with an existing transaction,
        // or create a new transaction.
        final PhaseContext<@NonNull ?> current = PhaseTracker.SERVER.getPhaseContext();
        if (current.getTransactor().logTileRemoval(tileentity, () -> (ServerLevel) (Object) this)) {
            final TileEntityPipeline pipeline = TileEntityPipeline.kickOff((ServerLevel) (Object) this, immutable)
                .addEffect(RemoveTileEntityFromWorldEffect.getInstance())
                .addEffect(RemoveTileEntityFromChunkEffect.getInstance())
                .build();
            pipeline.processEffects(current, new PipelineCursor(tileentity.getBlockState(), 0,immutable, tileentity, (Entity) null, Constants.World.DEFAULT_BLOCK_CHANGE_LIMIT));
            return;
        }
        super.shadow$removeBlockEntity(immutable);
    }

    @SuppressWarnings({"ConstantConditions", "RedundantCast"})
    @Override
    public boolean shadow$addBlockEntity(final net.minecraft.world.level.block.entity.BlockEntity tileEntity) {
        if (this.bridge$isFake() || PhaseTracker.SERVER.getSidedThread() != Thread.currentThread()) {
            // If we're fake or not on the server thread, well, we could effectively call
            // out whoever is trying to remove tile entities asynchronously....
            return super.shadow$addBlockEntity(tileEntity);
        }
        // Otherwise, let's go on and check if we're recording transactions,
        // and if so, log the tile entity removal (may associate with an existing transaction,
        // or create a new transaction.
        final PhaseContext<@NonNull ?> current = PhaseTracker.SERVER.getPhaseContext();
        if (current.doesBlockEventTracking()) {
            final BlockPos immutable = tileEntity.getBlockPos().immutable();
            if (tileEntity.getLevel() != (ServerLevel) (Object) this) {
                tileEntity.setLevelAndPosition((ServerLevel) (Object) this, immutable);
            }
            final ChunkAccess iChunk = this.shadow$getChunk(immutable.getX() >> 4, immutable.getZ() >> 4, ChunkStatus.FULL, false);
            if (!(iChunk instanceof LevelChunk)) {
                return super.shadow$addBlockEntity(tileEntity);
            }
            final LevelChunk chunk = this.shadow$getChunkAt(immutable);
            if (current.getTransactor().logTileAddition(tileEntity, () -> (ServerLevel) (Object) this, chunk)) {
                final TileEntityPipeline pipeline = TileEntityPipeline.kickOff((ServerLevel) (Object) this, immutable)
                    .addEffect(AddTileEntityToWorldWhileProcessingEffect.getInstance())
                    .addEffect(AddTileEntityToLoadedListInWorldEffect.getInstance())
                    .addEffect(AddTileEntityToTickableListEffect.getInstance())
                    .addEffect(TileOnLoadDuringAddToWorldEffect.getInstance())
                    .build();
                return pipeline.processEffects(current, new PipelineCursor(tileEntity.getBlockState(), 0, immutable, tileEntity,
                    (Entity) null,
                    Constants.World.DEFAULT_BLOCK_CHANGE_LIMIT));
            }
        }

        return super.shadow$addBlockEntity(tileEntity);
    }

    @SuppressWarnings({"RedundantCast", "ConstantConditions"})
    @Override
    public void shadow$setBlockEntity(final BlockPos pos, final net.minecraft.world.level.block.entity.@Nullable BlockEntity proposed) {
        final BlockPos immutable = pos.immutable();
        if (this.bridge$isFake() || PhaseTracker.SERVER.getSidedThread() != Thread.currentThread()) {
            // If we're fake or not on the server thread, well, we could effectively call
            // out whoever is trying to remove tile entities asynchronously....
            super.shadow$setBlockEntity(pos, proposed);
            return;
        }
        if (proposed != null) {
            if (proposed.getLevel() != (ServerLevel) (Object) this) {
                proposed.setLevelAndPosition((ServerLevel) (Object) this, immutable);
            } else {
                proposed.setPosition(pos);
            }
        }
        // Otherwise, let's go on and check if we're recording transactions,
        // and if so, log the tile entity removal (may associate with an existing transaction,
        // or create a new transaction.
        final PhaseContext<@NonNull ?> current = PhaseTracker.SERVER.getPhaseContext();
        if (current.doesBlockEventTracking()) {
            final net.minecraft.world.level.block.entity.@Nullable BlockEntity existing = this.shadow$getChunkAt(immutable).getBlockEntity(immutable);
            if (current.getTransactor().logTileReplacement(immutable, existing, proposed, () -> (ServerLevel) (Object) this)) {
                final TileEntityPipeline pipeline = TileEntityPipeline.kickOff((ServerLevel) (Object) this, immutable)
                    .addEffect(RemoveProposedTileEntitiesDuringSetIfWorldProcessingEffect.getInstance())
                    .addEffect(ReplaceTileEntityInWorldEffect.getInstance())
                    .build();
                pipeline.processEffects(current, new PipelineCursor(proposed.getBlockState(), 0,immutable, proposed, (Entity) null, Constants.World.DEFAULT_BLOCK_CHANGE_LIMIT));
                return;
            }
        }

        super.shadow$setBlockEntity(immutable, proposed);
    }

    @Override
    public void shadow$neighborChanged(final BlockPos pos, final Block blockIn, final BlockPos fromPos) {
        final BlockPos immutableTarget = pos.immutable();
        final BlockPos immutableFrom = fromPos.immutable();
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
                .log(SpongeCommon.logger(), Level.ERROR);
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
        final LevelChunk targetChunk = this.shadow$getChunkAt(immutableTarget);
        final BlockState targetBlockState = targetChunk.getBlockState(immutableTarget);
        // Sponge - Shortcircuit if the block has no neighbor logic
        if (!((TrackedBlockBridge) targetBlockState.getBlock()).bridge$overridesNeighborNotificationLogic()) {
            return;
        }
        // Sponge End

        // Sponge start - prepare notification
        final PhaseContext<@NonNull ?> peek = server.getPhaseContext();

        //  try { // Vanilla - We need to push the effect transactor so that it always pops
        try {
            final Supplier<ServerLevel> worldSupplier = VolumeStreamUtils.createWeaklyReferencedSupplier((ServerLevel) (Object) this, "ServerWorld");
            final net.minecraft.world.level.block.entity.@Nullable BlockEntity existingTile = targetChunk.getBlockEntity(
                immutableTarget,
                LevelChunk.EntityCreationType.CHECK
            );
            peek.getTransactor().logNeighborNotification(worldSupplier, immutableFrom, blockIn, immutableTarget, targetBlockState, existingTile);

            peek.associateNeighborStateNotifier(immutableFrom, targetBlockState.getBlock(), immutableTarget, ((ServerLevel) (Object) this), PlayerTracker.Type.NOTIFIER);
            // Sponge End

            targetBlockState.neighborChanged(((ServerLevel) (Object) this), immutableTarget, blockIn, immutableFrom, false);

        } catch (final Throwable throwable) {
            final CrashReport crashreport = CrashReport.forThrowable(throwable, "Exception while updating neighbours");
            final CrashReportCategory crashreportcategory = crashreport.addCategory("Block being updated");
            crashreportcategory.setDetail("Source block type", () -> {
                try {
                    return String.format("ID #%d (%s // %s)", Registry.BLOCK.getId(blockIn),
                        blockIn.getDescriptionId(), blockIn.getClass().getCanonicalName());
                } catch (final Throwable var2) {
                    return "ID #" + Registry.BLOCK.getId(blockIn);
                }
            });
            CrashReportCategory.populateBlockDetails(crashreportcategory, immutableTarget, targetBlockState);
            throw new ReportedException(crashreport);
        }
    }

    @Inject(method = "addEntity(Lnet/minecraft/world/entity/Entity;)Z",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getX()D"),
        cancellable = true
    )
    private void tracker$throwPreEventAndRecord(final Entity entityIn, final CallbackInfoReturnable<Boolean> cir) {
        if (this.bridge$isFake()) {
            return;
        }
        final PhaseTracker tracker = PhaseTracker.SERVER;
        if (tracker.getSidedThread() != Thread.currentThread()) {
            // TODO - async entity spawn logging
            return;
        }

        final Cause currentCause = tracker.currentCause();

        final SpawnEntityEvent.Pre pre = SpongeEventFactory.createSpawnEntityEventPre(
            currentCause,
            Collections.singletonList((org.spongepowered.api.entity.Entity) entityIn)
        );
        Sponge.eventManager().post(pre);
        if (pre.isCancelled()) {
            cir.setReturnValue(false);
        }

        final PhaseContext<@NonNull ?> current = tracker.getPhaseContext();

        if (current.allowsBulkEntityCaptures()) {
            current.getTransactor().logEntitySpawn(current, this, entityIn);
        }

    }
}
