package org.spongepowered.common.mixin.tracker.world.server;

import co.aikar.timings.Timing;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerTickList;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.bridge.TimingBridge;
import org.spongepowered.common.bridge.entity.EntityBridge;
import org.spongepowered.common.bridge.util.math.BlockPosBridge;
import org.spongepowered.common.bridge.world.TrackedWorldBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.bridge.world.chunk.ServerChunkProviderBridge;
import org.spongepowered.common.event.tracking.BlockChangeFlagManager;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.ScheduledBlockChange;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.context.SpongeProxyBlockAccess;
import org.spongepowered.common.event.tracking.phase.tick.TickPhase;
import org.spongepowered.common.mixin.tracker.world.WorldMixin_Tracker;
import org.spongepowered.common.util.VecHelper;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin_Tracker extends WorldMixin_Tracker implements TrackedWorldBridge {

    private SpongeProxyBlockAccess tracker$proxyBlockAccess = new SpongeProxyBlockAccess(this);
    private LinkedBlockingDeque<ScheduledBlockChange> tracker$scheduledChanges = new LinkedBlockingDeque<>();

    @Override
    public SpongeProxyBlockAccess bridge$getProxyAccess() {
        return this.tracker$proxyBlockAccess;
    }


    @Shadow public abstract ServerChunkProvider shadow$getChunkProvider();

    @Inject(method = "onEntityAdded", at = @At("TAIL"))
    private void onEntityAddedToWorldMarkAsTracked(final net.minecraft.entity.Entity entityIn, final CallbackInfo ci) {
        if (!this.bridge$isFake()) { // Only set the value if the entity is not fake
            ((EntityBridge) entityIn).bridge$setWorldTracked(true);
        }
    }

    @Inject(method = "onEntityRemoved", at = @At("TAIL"))
    private void onEntityRemovedFromWorldMarkAsUntracked(final net.minecraft.entity.Entity entityIn, final CallbackInfo ci) {
        if (!this.bridge$isFake() || ((EntityBridge) entityIn).bridge$isWorldTracked()) {
            ((EntityBridge) entityIn).bridge$setWorldTracked(false);
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
                    target = "Lnet/minecraft/world/server/ServerWorld;func_217390_a(Ljava/util/function/Consumer;Lnet/minecraft/entity/Entity;)V"),
            slice = @Slice(
                    from = @At(value = "INVOKE",
                            target = "Lnet/minecraft/world/server/ServerWorld;resetUpdateEntityTick()V"),
                    to = @At(value = "INVOKE",
                            target = "Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;int2ObjectEntrySet()Lit/unimi/dsi/fastutil/objects/ObjectSet;")
            )
    )
    private void tracker$wrapGlobalEntityTicking(final ServerWorld serverWorld, final Consumer<Entity> consumer, final Entity entity) {
        final PhaseContext<?> currentContext = PhaseTracker.SERVER.getCurrentContext();
        if (currentContext.state.alreadyCapturingEntityTicks()) {
            this.func_217390_a(consumer, entity);
            return;
        }
        TrackingUtil.tickkGlobalEntity(consumer, entity);
        // TODO - determine if updating rotation is needed.
    }

    @Redirect(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/server/ServerWorld;func_217390_a(Ljava/util/function/Consumer;Lnet/minecraft/entity/Entity;)V"),
            slice = @Slice(
                    from = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/profiler/IProfiler;startSection(Ljava/lang/String;)V", args = "stringValue=tick"),
                    to = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/profiler/IProfiler;startSection(Ljava/lang/String;)V", args = "stringValue=remove")
            )
    )
    private void tracker$wrapNormalEntityTick(final ServerWorld serverWorld, final Consumer<Entity> entityUpdateConsumer, final Entity entity) {
        final IPhaseState<?> currentState = PhaseTracker.SERVER.getCurrentState();
        if (currentState.alreadyCapturingEntityTicks()) {
            this.func_217390_a(entityUpdateConsumer, entity);
            return;
        }
        TrackingUtil.tickEntity(entityUpdateConsumer, entity);
    }


    @Override
    protected void impl$wrapTileEntityTick(final ITickableTileEntity tileEntity) {
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
     * @author gabizou - January 11th, 2020 - Minecraft 1.14.3
     * @reason For PhaseTracking, we need to wrap around the
     * {@link BlockState#tick(World, BlockPos, Random)} method, and the ScheduledTickList uses a lambda method
     * to {@code ServerWorld#tickBlock(NextTickListEntry)}, so it's either we customize the ScheduledTickList
     * or we wrap in this method here.
     *
     * @param blockState The block state being ticked
     * @param worldIn The world (this world)
     * @param pos The position of the block state
     * @param random The random (world.rand)
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Redirect(method = "tickBlock",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;tick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V"))
    private void impl$wrapBlockTick(final BlockState blockState, final World worldIn, final BlockPos pos, final Random random) {
        final PhaseContext<?> currentContext = PhaseTracker.SERVER.getCurrentContext();
        final IPhaseState currentState = currentContext.state;
        if (currentState.alreadyCapturingBlockTicks(currentContext) || currentState.ignoresBlockUpdateTick(currentContext)) {
            blockState.tick(worldIn, pos, random);
            return;
        }
        TrackingUtil.updateTickBlock(this, blockState, pos, random);
    }

    /**
     * @author gabizou - January 11th, 2020 - Minecraft 1.14.3
     * @reason For PhaseTracking, we need to wrap around the
     * {@link BlockState#tick(World, BlockPos, Random)} method, and the ScheduledTickList uses a lambda method
     * to {@code ServerWorld#tickBlock(NextTickListEntry)}, so it's either we customize the ScheduledTickList
     * or we wrap in this method here.
     *
     * @param blockState The block state being ticked
     * @param worldIn The world (this world)
     * @param pos The position of the block state
     * @param random The random (world.rand)
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Redirect(method = "tickEnvironment",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;randomTick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V"))
    private void impl$wrapBlockRandomTick(final BlockState blockState, final World worldIn, final BlockPos pos, final Random random) {
        try (final Timing timing = ((TimingBridge) blockState.getBlock()).bridge$getTimingsHandler()) {
            timing.startTiming();
            final PhaseContext<?> context = PhaseTracker.getInstance().getCurrentContext();
            final IPhaseState phaseState = context.state;
            if (phaseState.alreadyCapturingBlockTicks(context)) {
                blockState.randomTick(worldIn, pos, this.rand);
            } else {
                TrackingUtil.randomTickBlock(this, blockState, pos, this.rand);
            }
        }
    }

    /**
     * @author gabizou - January 18th, 2020 - Minecraft 1.14.3
     * You're probably wondering why we're doing this... Well... Take a seat and grab a cup of coffee...
     */
    @SuppressWarnings("unused")
    @Inject(method = "tick",
            at = @At(value = "INVOKE",target = "Lnet/minecraft/world/ServerTickList;tick()V"),
            slice = @Slice(
                    from = @At(value = "FIELD",
                            target = "Lnet/minecraft/world/server/ServerWorld;pendingBlockTicks:Lnet/minecraft/world/ServerTickList;"),
                    to = @At(value = "FIELD",
                            target = "Lnet/minecraft/world/server/ServerWorld;pendingFluidTicks:Lnet/minecraft/world/ServerTickList;")
            )
    )
    private void tracker$dumpAsyncScheduledBlockChanges(final BooleanSupplier hasTimeLeft, final CallbackInfo ci) {
        // Grab the pending block changes from
        final ScheduledBlockChange tail = this.tracker$scheduledChanges.peekLast();
        if (tail == null) {
            return;
        }
        /*
        You remember being told to get a cup of coffee, here's why...
        Because we have the "tail" end of where we want to stop processing block changes,
        asynchronous threads can keep pushing new changes onto the dequeue, // Btw, Faith says "putting, not pushing" // Morph also says "offering" probably with a "key"
        but we basically want to put a "stop" to say "Hey, your new changes are too late, they'll be processed later."
        So, to do this, we have to iterate for the first elements up to the tail, while also checking that the queue
        is basically empty after we've processed the most recently polled element.

        Also, because BlockingDequeue does wait for populating, we have to check for peekFirst at the end of the block
        to avoid waiting for the next pollFirst.

         */
        for (ScheduledBlockChange blockChange = this.tracker$scheduledChanges.pollFirst();
             blockChange != null && this.tracker$scheduledChanges.peekFirst() != tail;
             blockChange = this.tracker$scheduledChanges.pollFirst()) {
            try (final PhaseContext<?> context = blockChange.context.buildAndSwitch()) {
                PhaseTracker.SERVER.setBlockState(this, blockChange.pos, blockChange.state, blockChange.flag);
            }
            // We must check that the dequeue doesn't have any remaining elements because if it is empty
            // after we polled, the poll will literally wait until the dequeue has new elements....
            if (this.tracker$scheduledChanges.peekFirst() == null) {
                break;
            }
        }
    }



    /**
     * @author gabizou - August 4th, 2016
     * @author blood - May 11th, 2017 - Forces chunk requests if TE is ticking.
     * @reason Rewrites the check to be inlined to {@link BlockPosBridge}.
     *
     * @param pos The position
     * @return The block state at the desired position
     */
    @Override
    public net.minecraft.block.BlockState getBlockState(final BlockPos pos) {
        // Sponge - Replace with inlined method
        // if (this.isOutsideBuildHeight(pos)) // Vanilla
        if (((BlockPosBridge) pos).bridge$isInvalidYPosition()) {
            // Sponge end
            return Blocks.AIR.getDefaultState();
        }
        // ExtraUtilities 2 expects to get the proper chunk while mining or it gets stuck in infinite loop
        // TODO add TE config to disable/enable chunk loads
        final boolean forceChunkRequests = ((ServerChunkProviderBridge) this.shadow$getChunkProvider()).bridge$getForceChunkRequests();
        final PhaseTracker phaseTracker = PhaseTracker.getInstance();
        final IPhaseState<?> currentState = phaseTracker.getCurrentState();
        final boolean entered = currentState == TickPhase.Tick.TILE_ENTITY;
        if (entered) {
            ((ServerChunkProviderBridge) this.shadow$getChunkProvider()).bridge$setForceChunkRequests(true);
        }
        try {
            // Proxies have block changes for bulk special captures
            final net.minecraft.block.BlockState blockState = this.tracker$proxyBlockAccess.getBlockState(pos);
            if (blockState != null) {
                return blockState;
            }
            final IChunk chunk= this.shadow$getChunkAt(pos);
            return chunk.getBlockState(pos);
        } finally {
            if (entered) {
                ((ServerChunkProviderBridge) this.shadow$getChunkProvider()).bridge$setForceChunkRequests(forceChunkRequests);
            }
        }
    }



    /**
     * @author gabizou, March 12th, 2016
     *
     * Move this into WorldServer as we should not be modifying the client world.
     *
     * Purpose: Rewritten to support capturing blocks
     */
    @Override
    public boolean setBlockState(final BlockPos pos, final net.minecraft.block.BlockState newState, final int flags) {
        if (!WorldMixin_Tracker.shadow$isOutsideBuildHeight(pos)) {
            return false;
        } else if (this.worldInfo.getGenerator() == WorldType.DEBUG_ALL_BLOCK_STATES) { // isRemote is always false since this is WorldServer
            return false;
        } else {
            // Sponge - reroute to the PhaseTracker
            return PhaseTracker.getInstance().setBlockState(this, pos.toImmutable(), newState, BlockChangeFlagManager.fromNativeInt(flags));
        }
    }

    @Override
    public SpongeBlockSnapshot bridge$createSnapshot(final net.minecraft.block.BlockState state, final BlockPos pos, final BlockChangeFlag updateFlag) {
        final SpongeBlockSnapshotBuilder builder = SpongeBlockSnapshotBuilder.pooled();
        builder.reset();
        builder.blockState(state)
                .worldId(((WorldProperties) this.worldInfo).getUniqueId())
                .position(VecHelper.toVector3i(pos));
        final Chunk chunk = this.shadow$getChunkAt(pos);
        if (chunk == null) {
            final SpongeBlockSnapshot build = builder.flag(updateFlag).build();
            builder.reset();
            return build;
        }
        final Optional<UUID> creator = ((ChunkBridge) chunk).bridge$getBlockOwnerUUID(pos);
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
                }
                catch(final Throwable t) {
                    // ignore
                }
            }
        }
        builder.flag(updateFlag);
        return builder.build();
    }

    @Override
    public BlockingQueue<ScheduledBlockChange> bridge$getScheduledBlockChangeList() {
        return this.tracker$scheduledChanges;
    }
}
