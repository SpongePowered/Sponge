package org.spongepowered.common.mixin.tracker.world.server;

import co.aikar.timings.Timing;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.TimingBridge;
import org.spongepowered.common.bridge.entity.EntityBridge;
import org.spongepowered.common.bridge.util.math.BlockPosBridge;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.bridge.world.chunk.ServerChunkProviderBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.BlockChangeFlagManager;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.tick.TickPhase;
import org.spongepowered.common.mixin.tracker.world.WorldMixin_Tracker;

import java.util.Random;
import java.util.function.Consumer;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin_Tracker extends WorldMixin_Tracker implements ServerWorldBridge {


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
            final net.minecraft.block.BlockState blockState = this.impl$proxyBlockAccess.getBlockState(pos);
            if (blockState != null) {
                return blockState;
            }
            final IChunk chunk= this.getChunk(pos);
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
        if (!this.isValid(pos)) {
            return false;
        } else if (this.worldInfo.getGenerator() == WorldType.DEBUG_ALL_BLOCK_STATES) { // isRemote is always false since this is WorldServer
            return false;
        } else {
            // Sponge - reroute to the PhaseTracker
            return PhaseTracker.getInstance().setBlockState(this, pos.toImmutable(), newState, BlockChangeFlagManager.fromNativeInt(flags));
        }
    }

    /**
     * @author gabizou - July 25th, 2018
     * @reason Technically an overwrite for {@link World#destroyBlock(BlockPos, boolean)}
     * so that we can artificially capture/associate entity spawns from the proposed block
     * destruction when the actual block event is thrown, whether captures are taking
     * place or not. In the context of "if block changes are not captured", we do still need
     * to associate the drops before the actual block is removed
     *
     * @param pos
     * @param dropBlock
     * @return
     */
    @Override
    public boolean destroyBlock(final BlockPos pos, final boolean dropBlock) {
        final net.minecraft.block.BlockState iblockstate = this.getBlockState(pos);
        final Block block = iblockstate.getBlock();

        if (iblockstate.getMaterial() == Material.AIR) {
            return false;
        }
        // Sponge Start - Fire the change block pre here, before we bother with drops. If the pre is cancelled, just don't bother.
        if (ShouldFire.CHANGE_BLOCK_EVENT_PRE) {
            if (SpongeCommonEventFactory.callChangeBlockEventPre(this, pos).isCancelled()) {
                return false;
            }
        }
        // Sponge End
        this.playEvent(2001, pos, Block.getStateId(iblockstate));

        if (dropBlock) {
            // Sponge Start - since we are going to perform block drops, we need
            // to notify the current phase state and find out if capture pos is to be used.
            final PhaseContext<?> context = PhaseTracker.getInstance().getCurrentContext();
            final IPhaseState<?> state = PhaseTracker.getInstance().getCurrentState();
            final boolean isCapturingBlockDrops = state.alreadyProcessingBlockItemDrops();
            final BlockPos previousPos;
            if (isCapturingBlockDrops) {
                previousPos = context.getCaptureBlockPos().getPos().orElse(null);
                context.getCaptureBlockPos().setPos(pos);
            } else {
                previousPos = null;
            }
            // Sponge End
            block.dropBlockAsItem((ServerWorld) (Object) this, pos, iblockstate, 0);
            // Sponge Start
            if (isCapturingBlockDrops) {
                // we need to reset the capture pos because we've been capturing item and entity drops this way.
                context.getCaptureBlockPos().setPos(previousPos);
            }
            // Sponge End

        }

        // Sponge - reduce the call stack by calling the more direct method.
        if (!this.isValid(pos)) {
            return false;
        } else if (this.worldInfo.getGenerator() == WorldType.DEBUG_ALL_BLOCK_STATES) { // isRemote is always false since this is WorldServer
            return false;
        } else {
            // Sponge - reroute to the PhaseTracker
            return PhaseTracker.getInstance().setBlockState(this, pos.toImmutable(), Blocks.AIR.getDefaultState(), BlockChangeFlags.ALL);
        }
    }

}
