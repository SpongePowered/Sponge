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
package org.spongepowered.common.event.tracking;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.MapMaker;
import net.minecraft.block.Block;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.bridge.OwnershipTrackedBridge;
import org.spongepowered.common.bridge.block.BlockBridge;
import org.spongepowered.common.bridge.entity.EntityBridge;
import org.spongepowered.common.bridge.world.TrackedWorldBridge;
import org.spongepowered.common.bridge.world.chunk.TrackedChunkBridge;
import org.spongepowered.common.config.category.PhaseTrackerCategory;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.phase.general.GeneralPhase;
import org.spongepowered.common.event.tracking.phase.tick.NeighborNotificationContext;
import org.spongepowered.common.event.tracking.phase.tick.TickPhase;
import org.spongepowered.common.accessor.world.server.ServerWorldAccessor;
import org.spongepowered.common.registry.builtin.sponge.SpawnTypeStreamGenerator;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.world.SpongeBlockChangeFlag;
import org.spongepowered.common.world.SpongeLocatableBlockBuilder;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The core state machine of Sponge. Acts a as proxy between various engine objects by processing actions through
 * various {@link IPhaseState}s.
 */
@SuppressWarnings("unchecked")
public final class PhaseTracker {
    public static final PhaseTracker CLIENT = new PhaseTracker();
    public static final PhaseTracker SERVER = new PhaseTracker();

    private static final Map<Thread, PhaseTracker> SPINOFF_TRACKERS = new MapMaker().weakKeys().concurrencyLevel(8).makeMap();

    public void init() {
        if (this != PhaseTracker.SERVER) {
            return;
        }
        if (this.hasRun) {
            return;
        }
        this.hasRun = true;
        Task.builder()
                .name("Sponge Async To Sync Entity Spawn Task")
                .intervalTicks(1)
                .execute(() -> {
                    if (PhaseTracker.ASYNC_CAPTURED_ENTITIES.isEmpty()) {
                        return;
                    }

                    final List<net.minecraft.entity.Entity> entities = new ArrayList<>(PhaseTracker.ASYNC_CAPTURED_ENTITIES);
                    PhaseTracker.ASYNC_CAPTURED_ENTITIES.removeAll(entities);
                    try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                        // We are forcing the spawn, as we can't throw the proper event at the proper time, so
                        // we'll just mark it as "forced".
                        frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypeStreamGenerator.FORCED);
                        for (net.minecraft.entity.Entity entity : entities) {
                            // At this point, we don't care what the causes are...
                            PhaseTracker.getInstance().spawnEntityWithCause((World<?>) entity.getEntityWorld(), (Entity) entity);
                        }
                    }

                })
                .plugin(SpongeImpl.getPlugin())
                .build();
    }

    @Nullable private WeakReference<Thread> sidedThread;
    private boolean hasRun = false;


    public void setThread(@Nullable final Thread thread) throws IllegalAccessException {
        final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        if ((stackTrace.length < 3)) {
            throw new IllegalAccessException("Cannot call directly to change thread.");
        }
        if (this != PhaseTracker.SERVER && this != PhaseTracker.CLIENT && this.sidedThread == null) {
            this.sidedThread = new WeakReference<>(thread);
            return;
        }

        final String callingClass = stackTrace[1].getClassName();
        final String callingParent = stackTrace[2].getClassName();
        if (
            !(
                (Constants.MINECRAFT_CLIENT.equals(callingClass) && Constants.MINECRAFT_CLIENT.equals(callingParent))
                || (Constants.MINECRAFT_SERVER.equals(callingClass) && Constants.MINECRAFT_SERVER.equals(callingParent))
                || (Constants.DEDICATED_SERVER.equals(callingClass) && Constants.MINECRAFT_CLIENT.equals(callingParent))
                || (Constants.INTEGRATED_SERVER.equals(callingClass) && Constants.MINECRAFT_CLIENT.equals(callingParent))
            )
        ) {
            throw new IllegalAccessException("Illegal Attempts to re-assign PhaseTracker threads on Sponge");
        }

        this.sidedThread = new WeakReference<>(thread);

    }

    @Nullable
    public Thread getSidedThread() {
        return this.sidedThread != null ? this.sidedThread.get() : null;
    }

    public static PhaseTracker getInstance() {
        final Thread current = Thread.currentThread();
        if (current == PhaseTracker.SERVER.getSidedThread()) {
            return PhaseTracker.SERVER;
        }
        if (current == PhaseTracker.CLIENT.getSidedThread()) {
            return PhaseTracker.CLIENT;
        }

        return PhaseTracker.SPINOFF_TRACKERS.computeIfAbsent(current, (thread) -> {
            try {
                final PhaseTracker phaseTracker = new PhaseTracker();
                phaseTracker.setThread(thread);
                return phaseTracker;
            } catch (final IllegalAccessException e) {
                throw new RuntimeException("Unable to create a new PhaseTracker for Thread: " + thread, e);
            }
        });
    }

    static final CopyOnWriteArrayList<net.minecraft.entity.Entity> ASYNC_CAPTURED_ENTITIES = new CopyOnWriteArrayList<>();

    final PhaseStack stack = new PhaseStack();

    // ----------------- STATE ACCESS ----------------------------------

    @SuppressWarnings("rawtypes")
    void switchToPhase(final IPhaseState<?> state, final PhaseContext<?> phaseContext) {
        if (phaseContext.createdTracker != this && Thread.currentThread() != this.getSidedThread()) {
            // lol no, report the block change properly
            new PrettyPrinter(60).add("Illegal Async PhaseTracker Access").centre().hr()
                .addWrapped(PhasePrinter.ASYNC_TRACKER_ACCESS)
                .add()
                .add(new Exception("Async Block Change Detected"))
                .log(SpongeImpl.getLogger(), Level.ERROR);
            // Maybe? I don't think this is wise.
            return;
        }
        checkNotNull(state, "State cannot be null!");
        checkNotNull(phaseContext, "PhaseContext cannot be null!");
        checkArgument(phaseContext.isComplete(), "PhaseContext must be complete!");
        if (SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().isVerbose()) {
            if (this.stack.size() > 6) {
                if (this.stack.checkForRunaways(state, phaseContext)) {
                    PhasePrinter.printRunawayPhase(this.stack, state, phaseContext);
                }

            }
        }

        if (Sponge.isServerAvailable() && ((IPhaseState) state).shouldProvideModifiers(phaseContext)) {
            SpongeImpl.getCauseStackManager().registerPhaseContextProvider(phaseContext);
        }
        this.stack.push(state, phaseContext);
    }

    @SuppressWarnings({"rawtypes", "unused", "try"})
    void completePhase(final PhaseContext<?> context) {
        if (context.createdTracker != this && Thread.currentThread() != this.getSidedThread()) {
            // lol no, report the block change properly
            new PrettyPrinter(60).add("Illegal Async PhaseTracker Access").centre().hr()
                .addWrapped(PhasePrinter.ASYNC_TRACKER_ACCESS)
                .add()
                .add(new Exception("Async Block Change Detected"))
                .log(SpongeImpl.getLogger(), Level.ERROR);
            return;
        }
        final PhaseContext<?> currentContext = this.stack.peek();
        final IPhaseState<?> state = currentContext.state;
        final boolean isEmpty = this.stack.isEmpty();
        if (isEmpty) {
            // The random occurrence that we're told to complete a phase
            // while a world is being changed unknowingly.
            PhasePrinter.printEmptyStackOnCompletion(currentContext);
            return;
        }

        if (context.state != state) {
            PhasePrinter.printIncorrectPhaseCompletion(this.stack, context.state, state);

            // The phase on the top of the stack was most likely never completed.
            // Since we don't know when and where completePhase was intended to be called for it,
            // we simply pop it to allow processing to continue (somewhat) as normal
            this.stack.pop();
            return;
        }

        if (SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().isVerbose() ) {
            if (this.stack.checkForRunaways(GeneralPhase.Post.UNWINDING, null)) {
                // This printing is to detect possibilities of a phase not being cleared properly
                // and resulting in a "runaway" phase state accumulation.
                PhasePrinter.printRunnawayPhaseCompletion(this.stack, state);
            }
        }

        final boolean hasCaptures = currentContext.hasCaptures();
        try (final UnwindingPhaseContext unwinding = UnwindingPhaseContext.unwind(state, currentContext, hasCaptures) ) {
            // With UnwindingPhaseContext#unwind checking for post, if it is null, the try
            // will not attempt to close the phase context. If it is required,
            // it already automatically pushes onto the phase stack, along with
            // a new list of capture lists
            try { // Yes this is a nested try, but in the event the current phase cannot be unwound,
                // at least unwind UNWINDING to process any captured objects so we're not totally without
                // loss of objects
                if (hasCaptures) {
                    ((IPhaseState) state).unwind(currentContext);
                }
            } catch (final Exception e) {
                PhasePrinter.printMessageWithCaughtException(this.stack, "Exception Exiting Phase", "Something happened when trying to unwind", state, currentContext, e);
            }
        } catch (final Exception e) {
            PhasePrinter.printMessageWithCaughtException(this.stack, "Exception Post Dispatching Phase", "Something happened when trying to post dispatch state", state,
                currentContext, e);
        }
        this.checkPhaseContextProcessed(state, currentContext);
        // If pop is called, the Deque will already throw an exception if there is no element
        // so it's an error properly handled.
        this.stack.pop();

        if (this.stack.isEmpty()) {
            for (final org.spongepowered.api.world.server.ServerWorld apiWorld : SpongeImpl.getWorldManager().getWorlds()) {
                final TrackedWorldBridge trackedWorld = (TrackedWorldBridge) apiWorld;
                if (trackedWorld.bridge$getProxyAccess().hasProxy()) {
                    new PrettyPrinter().add("BlockPRoxy has extra proxies not pruned!").centre().hr()
                        .add("When completing the Phase: %s, some foreign BlockProxy was pushed, but never pruned.", state)
                        .add()
                        .add("Please analyze the following exception from the proxy:")
                        .add(new Exception())
                        .print(System.err);

                }
            }
        }

    }


    private void checkPhaseContextProcessed(final IPhaseState<?> state, final PhaseContext<?> context) {
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().isVerbose() && PhasePrinter.printedExceptionsForUnprocessedState.contains(state)) {
            return;
        }

        if (context.notAllCapturesProcessed()) {
            PhasePrinter.printUnprocessedPhaseContextObjects(this.stack, state, context);
            PhasePrinter.printedExceptionsForUnprocessedState.add(state);

        }
    }


    public static Block validateBlockForNeighborNotification(final ServerWorld worldServer, final BlockPos pos, @Nullable Block blockIn,
        final BlockPos otherPos, final Chunk chunk) {
        if (blockIn == null) {
            // If the block is null, check with the PhaseState to see if it can perform a safe way
            final PhaseContext<?> currentContext = PhaseTracker.getInstance().getCurrentContext();
            final PhaseTrackerCategory trackerConfig = SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker();

            if (currentContext.state == TickPhase.Tick.TILE_ENTITY) {
                // Try to save ourselves
                final TileEntity source = (TileEntity) currentContext.getSource();

                final TileEntityType<?> type = Optional.ofNullable(source)
                        .map(TileEntity::getType)
                        .orElse(null);
                if (type != null) {
                    ResourceLocation id = TileEntityType.getId(type);
                    if (id == null) {
                        id = new ResourceLocation(source.getClass().getCanonicalName());
                    }
                    final Map<String, Boolean> autoFixedTiles = trackerConfig.getAutoFixedTiles();
                    final boolean contained = autoFixedTiles.containsKey(type.toString());
                    // If we didn't map the tile entity yet, we should apply the mapping
                    // based on whether the source is the same as the TileEntity.
                    if (!contained) {
                        if (pos.equals(source.getPos())) {
                            autoFixedTiles.put(id.toString(), true);
                        } else {
                            autoFixedTiles.put(id.toString(), false);
                        }
                    }
                    final boolean useTile = contained && autoFixedTiles.get(id.toString());
                    if (useTile) {
                        blockIn = source.getBlockState().getBlock();
                    } else {
                        blockIn = (pos.getX() >> 4 == chunk.getPos().x && pos.getZ() >> 4 == chunk.getPos().z)
                                  ? chunk.getBlockState(pos).getBlock()
                                  : worldServer.getBlockState(pos).getBlock();
                    }
                    if (!contained && trackerConfig.isReportNullSourceBlocks()) {
                        PhasePrinter.printNullSourceBlockWithTile(pos, blockIn, otherPos, id, useTile, new NullPointerException("Null Source Block For TileEntity Neighbor Notification"));
                    }
                } else {
                    blockIn = (pos.getX() >> 4 == chunk.getPos().x && pos.getZ() >> 4 == chunk.getPos().z)
                              ? chunk.getBlockState(pos).getBlock()
                              : worldServer.getBlockState(pos).getBlock();
                    if (trackerConfig.isReportNullSourceBlocks()) {
                        PhasePrinter.printNullSourceBlockNeighborNotificationWithNoTileSource(pos, blockIn, otherPos,
                            new NullPointerException("Null Source Block For Neighbor Notification"));
                    }
                }

            } else {
                blockIn = (pos.getX() >> 4 == chunk.getPos().x && pos.getZ() >> 4 == chunk.getPos().z)
                          ? chunk.getBlockState(pos).getBlock()
                          : worldServer.getBlockState(pos).getBlock();
                if (trackerConfig.isReportNullSourceBlocks()) {
                    PhasePrinter.printNullSourceForBlock(worldServer, pos, blockIn, otherPos, new NullPointerException("Null Source Block For Neighbor Notification"));
                }
            }
        }
        return blockIn;
    }


    String dumpStack() {
        if (this.stack.isEmpty()) {
            return "[Empty stack]";
        }

        final PrettyPrinter printer = new PrettyPrinter(40);
        this.stack.forEach(data -> PhasePrinter.PHASE_PRINTER.accept(printer, data));

        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        printer.print(new PrintStream(stream));

        return stream.toString();
    }

    // ----------------- SIMPLE GETTERS --------------------------------------

    public IPhaseState<?> getCurrentState() {
        if (Thread.currentThread() != this.getSidedThread()) {
            throw new UnsupportedOperationException("Cannot access the PhaseTracker off-thread, please use the respective PhaseTracker for their proper thread.");
        }
        return this.stack.peekState();
    }

    public PhaseContext<?> getCurrentContext() {
        if (Thread.currentThread() != this.getSidedThread()) {
            throw new UnsupportedOperationException("Cannot access the PhaseTracker off-thread, please use the respective PhaseTracker for their proper thread.");
        }
        return this.stack.peekContext();
    }

    // --------------------- DELEGATED WORLD METHODS -------------------------

    /**
     * Replacement of {@link net.minecraft.world.World#neighborChanged(BlockPos, Block, BlockPos)}
     * that adds tracking into play.
     *
     *  @param mixinWorld THe world
     * @param notifyPos The original notification position
     * @param sourceBlock The source block type
     * @param sourcePos The source block position
     * @param isMoving If the block is moving
     */
    @SuppressWarnings("rawtypes")
    public void notifyBlockOfStateChange(final TrackedWorldBridge mixinWorld, final net.minecraft.block.BlockState notifyState, final BlockPos notifyPos,
        final Block sourceBlock, final BlockPos sourcePos, final boolean isMoving) {
        if (!SpongeImplHooks.onServerThread()) {
            // lol no, report the block change properly
            new PrettyPrinter(60).add("Illegal Async PhaseTracker Access").centre().hr()
                .addWrapped(PhasePrinter.ASYNC_TRACKER_ACCESS)
                .add()
                .add(new Exception("Async Block Notifcation Detected"))
                .log(SpongeImpl.getLogger(), Level.ERROR);
            // Maybe? I don't think this is wise to try and sync back a notification on the main thread.
            return;
        }
        try {
            // Sponge start - prepare notification
            final PhaseContext<?> peek = this.stack.peek();
            final IPhaseState state = peek.state;
            if (!((BlockBridge) notifyState.getBlock()).bridge$hasNeighborChangedLogic()) {
                // A little short-circuit so we do not waste expense to call neighbor notifications on blocks that do
                // not override the method neighborChanged
                return;
            }
            // If the phase state does not want to allow neighbor notifications to leak while processing,
            // it needs to be able to do so. It will replay the notifications in the order in which they were received,
            // such that the notification will be sent out in the same order as the block changes that may have taken place.
            if ((ShouldFire.CHANGE_BLOCK_EVENT || ShouldFire.NOTIFY_NEIGHBOR_BLOCK_EVENT) && state.doesCaptureNeighborNotifications(peek)) {
                peek.getCapturedBlockSupplier().captureNeighborNotification(mixinWorld, notifyState, notifyPos, sourceBlock, sourcePos, isMoving);
                return;
            }
            state.associateNeighborStateNotifier(peek, sourcePos, notifyState.getBlock(), notifyPos, ((ServerWorld) mixinWorld), PlayerTracker.Type.NOTIFIER);
            final LocatableBlock block = new SpongeLocatableBlockBuilder()
                .world(((World) mixinWorld))
                .position(sourcePos.getX(), sourcePos.getY(), sourcePos.getZ())
                .state((BlockState) sourceBlock.getDefaultState()).build();
            try (final NeighborNotificationContext context = TickPhase.Tick.NEIGHBOR_NOTIFY.createPhaseContext(PhaseTracker.SERVER)
                .source(block)
                .sourceBlock(sourceBlock)
                .setNotifiedBlockPos(notifyPos)
                .setNotifiedBlockState(notifyState)
                .setSourceNotification(sourcePos)
                .allowsCaptures(state) // We need to pass the previous state so we don't capture blocks when we're in world gen.

            ) {
                // Since the notifier may have just been set from the previous state, we can
                // ask it to contribute to our state
                state.provideNotifierForNeighbors(peek, context);
                context.buildAndSwitch();  // We need to enter the phase state, otherwise if the context is not switched into,
                // the try with resources will perform a close without the phase context being entered, leading to issues of closing
                // other phase contexts.
                // Refer to https://github.com/SpongePowered/SpongeForge/issues/2706
                if (PhasePrinter.checkMaxBlockProcessingDepth(state, peek, context.getDepth())) {
                    return;
                }
                // Sponge End

                notifyState.neighborChanged(((ServerWorld) mixinWorld), notifyPos, sourceBlock, sourcePos, isMoving);
            }
        } catch (final Throwable throwable) {
            final CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception while updating neighbours");
            final CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being updated");
            crashreportcategory.addDetail("Source block type", () -> {
                try {
                    return String.format("ID #%d (%s // %s)", Registry.BLOCK.getId(sourceBlock),
                        sourceBlock.getTranslationKey(), sourceBlock.getClass().getCanonicalName());
                } catch (final Throwable var2) {
                    return "ID #" + Registry.BLOCK.getId(sourceBlock);
                }
            });
            CrashReportCategory.addBlockInfo(crashreportcategory, notifyPos, notifyState);
            throw new ReportedException(crashreport);
        }
    }

    /**
     * Replacement of {@link ServerWorld#setBlockState(BlockPos, net.minecraft.block.BlockState, int)}
     * with the additions of cause tracking
     *
     * @param pos The position of the block state to set
     * @param newState The new state
     * @param flag The notification flags
     * @return True if the block was successfully set (or captured)
     */
    @SuppressWarnings("rawtypes")
    public boolean setBlockState(final TrackedWorldBridge mixinWorld, final BlockPos pos,
                                 final net.minecraft.block.BlockState newState, final BlockChangeFlag flag) {
        if (!SpongeImplHooks.onServerThread()) {
            // lol no, report the block change properly
            try {
                PhaseTracker.SERVER.proposeScheduledBlockChange(this.getCurrentContext().defensiveCopy(PhaseTracker.SERVER), mixinWorld, pos, newState, flag);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
            PhasePrinter.printAsyncBlockChange(mixinWorld, pos, newState);
            return false;
        }
        final SpongeBlockChangeFlag spongeFlag = (SpongeBlockChangeFlag) flag;
        final ServerWorld world = (ServerWorld) mixinWorld;

        // World#setBlockState - A Sponge Story
        // Vanilla already has the `isOutsideBuildHeight(pos)` check
        //if (isOutsideBuildHeight(pos)) {
        //    return false;
        // Vanilla already checks if it's a server world and that the generator is not a debugger (otherwise we wouldn't
        // be here)
        //} else if (!this.isRemote && this.worldInfo.getGenerator() == WorldType.DEBUG_ALL_BLOCK_STATES) {
        //    return false;
        //} else {
        // Vanilla gets the chunk
        final Chunk chunk = world.getChunkAt(pos);
        // Sponge Start - double check the chunk is not empty.
        // It is now possible for setBlockState to be called on an empty chunk due to our optimization
        // for returning empty chunks when we don't want a chunk to load.
        // If chunk is empty, we simply return to avoid any further logic.
        if (chunk.isEmpty()) {
            return false;
        }
        // Sponge End
        final Block block = newState.getBlock();
        // Vanilla uses the bitwise flag, we use BlockChangeFlag.
        // Sponge Start - Since vanilla will at this point tell the chunk to set the state, we need
        // to start a tracking position

        final net.minecraft.block.BlockState currentState = chunk.getBlockState(pos);

        // We can allow the block to get changed, regardless how it's captured, not captured, etc.
        // because ChunkMixin will perform the necessary changes, and appropriately prevent any specific
        // physics handling.

        final TrackedChunkBridge mixinChunk = (TrackedChunkBridge) chunk;
        // Sponge - Use our mixin method that allows using the BlockChangeFlag.

        // Up until this point, we've been setting up sponge stuff, this next line is from vanilla
        // where it tells the chunk to set the new state, but we have to call our custom method
        // to do transaction handling
        // final net.minecraft.block.BlockState blockstate = chunk.setBlockState(pos, newState, (flags & 64) != 0);
        final net.minecraft.block.BlockState originalState = mixinChunk.bridge$setBlockState(pos, newState, currentState, spongeFlag);
        // Sponge End
        if (originalState == null) {
            return false;
        } // else { // Sponge - redundant else

        // blockstate1 -> newWorldState
        final net.minecraft.block.BlockState newWorldState = world.getBlockState(pos);
        if (newWorldState != originalState && (newWorldState.getOpacity(world, pos) != originalState.getOpacity(world, pos) || newWorldState.getLightValue() != originalState.getLightValue() || newWorldState.func_215691_g() || originalState.func_215691_g())) {
            // this.profiler.startSection("queueCheckLight");
            world.getProfiler().startSection("queueCheckLight");
            // this.getChunkProvider().getLightManager().checkBlock(pos);
            world.getChunkProvider().getLightManager().checkBlock(pos);
            // this.profiler.endSection();
            world.getProfiler().endSection();
        }

        // Sponge Start - At this point, we can stop and check for captures;
        //  by short circuiting here, we avoid additional block processing that would otherwise
        //  have potential side effects (and ChunkMixin#bridge$setBlockState does a wonderful job at avoiding
        //  unnecessary logic in those cases).

        final PhaseContext<?> context = this.stack.peek();
        final IPhaseState<?> phaseState = context.state;
        if (((IPhaseState) phaseState).doesBulkBlockCapture(context) && ShouldFire.CHANGE_BLOCK_EVENT) {
            // Basically at this point, there's nothing left for us to do since
            // ChunkMixin will capture the block change, and submit it to be
            // "captured". It's only when there's immediate block event
            // processing that we need to actually create the event and process
            // that transaction.
            return true;
        }

        // Since we don't do bulk capturing, we should also check if we are simply allowed to
        // throw an event, if we are, then we should do that process.
        if (((IPhaseState) phaseState).doesBlockEventTracking(context) && ShouldFire.CHANGE_BLOCK_EVENT) {
            try {
                // Fall back to performing a singular block capture and throwing an event with all the
                // repercussions, such as neighbor notifications and whatnot. Entity spawns should also be
                // properly handled since bulk captures technically should be disabled if reaching
                // this point.

                final SpongeBlockSnapshot originalBlockSnapshot = context.getSingleSnapshot();

                final Transaction<BlockSnapshot> transaction = TrackingUtil.TRANSACTION_CREATION.apply(originalBlockSnapshot).get();
                final ImmutableList<Transaction<BlockSnapshot>> transactions = ImmutableList.of(transaction);
                // Create and throw normal event
                final Cause currentCause = Sponge.getCauseStackManager().getCurrentCause();
                final ChangeBlockEvent normalEvent =
                        originalBlockSnapshot.blockChange.createEvent(currentCause, transactions);
                try (@SuppressWarnings("try") final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                    SpongeImpl.postEvent(normalEvent);
                    // We put the normal event at the end of the cause, still keeping in line with the
                    // API contract that the ChangeBlockEvnets are pushed to the cause for Post, but they
                    // will not replace the root causes. Likewise, this does not leak into the cause stack
                    // for plugin event listeners performing other operations that could potentially alter
                    // the cause stack (CauseStack:[Player, ScheduledTask] vs. CauseStack:[ChangeBlockEvent, Player, ScheduledTask])
                    final Cause normalizedEvent;
                    if (ShouldFire.CHANGE_BLOCK_EVENT_POST) {
                        normalizedEvent = currentCause.with(normalEvent);
                    } else {
                        normalizedEvent = currentCause;
                    }
                    if (normalEvent.isCancelled()) {
                        // If the normal event is cancelled, mark the transaction as invalid already
                        transaction.setValid(false);
                    }
                    final ChangeBlockEvent.Post post = ((IPhaseState) phaseState).createChangeBlockPostEvent(context, transactions, normalizedEvent);
                    if (ShouldFire.CHANGE_BLOCK_EVENT_POST) {
                        SpongeImpl.postEvent(post);
                    }
                    if (post.isCancelled()) {
                        // And finally, if the post event is cancelled, mark the transaction as invalid.
                        transaction.setValid(false);
                    }
                    if (!transaction.isValid()) {
                        transaction.getOriginal().restore(true, BlockChangeFlags.NONE);
                        if (((IPhaseState) phaseState).tracksBlockSpecificDrops(context)) {
                            ((PhaseContext) context).getBlockDropSupplier().removeAllIfNotEmpty(pos);
                        }
                        return false; // Short circuit
                    }
                    // And now, proceed as normal.
                    // If we've gotten this far, the transaction wasn't cancelled, so pass 'noCancelledTransactions' as 'true'
                    TrackingUtil.performTransactionProcess(transaction, context, 0);
                    return true;
                }
            } catch (final Exception | NoClassDefFoundError e) {
                PhasePrinter.printBlockTrackingException(this, context, phaseState, e);
                return false;
            }
        }
        // Sponge End - continue with vanilla mechanics

        // Vanilla does this whole block short circuit
        // Sponge Start - eliminate big block
        // if (newWorldState == newState) {
        if (newWorldState != newState) {
            return true;
        }
        // Sponge End
        if (originalState != newWorldState) {
            // this.func_225319_b(pos, originalState, newWorldState);
            world.func_225319_b(pos, originalState, newWorldState);
        }

        // Vanilla flags & 2 to check if clients are notified. isRemote is redundant since it's guaranteed a server world.
        // And the last bit is the equivalent to basically checking if the chunk is not a border and populated.
        // if ((flags & 2) != 0 && (!this.isRemote || (flags & 4) == 0) && (this.isRemote || chunk.getLocationType() != null && chunk.getLocationType().isAtLeast(ChunkHolder.LocationType.TICKING))) {
        if (spongeFlag.notifyClients() && (chunk.getLocationType() != null && chunk.getLocationType().isAtLeast(ChunkHolder.LocationType.TICKING))) {
            // this.notifyBlockUpdate(pos, blockstate, newWorldState, flags);
            world.notifyBlockUpdate(pos, originalState, newWorldState, spongeFlag.getRawFlag());
        }

        // Vanilla isremote is redundant
        // if (!this.isRemote && (flags & 1) != 0) {
        if (spongeFlag.updateNeighbors()) {
            // this.notifyNeighbors(pos, originalState.getBlock());
            world.notifyNeighbors(pos, originalState.getBlock());
            if (newWorldState.hasComparatorInputOverride()) {
                // this.updateComparatorOutputLevel(pos, block);
                world.updateComparatorOutputLevel(pos, block);
            }
        }

        // if ((flags & 16) == 0) { // Sponge BlockChangeFlag acknowledges this
        if (spongeFlag.notifyObservers()) {
            // final int i = flags & -2; // Vanilla negates 2 to flip the neighbor notification mask
            final int newFlag = spongeFlag.withUpdateNeighbors(false).getRawFlag();
            // blockstate.updateDiagonalNeighbors(this, pos, i);
            currentState.updateDiagonalNeighbors(world, pos, newFlag);
            // newWorldState.updateNeighbors(this, pos, i);
            newWorldState.updateNeighbors(world, pos, newFlag);
            // newWorldState.updateDiagonalNeighbors(this, pos, i);
            newWorldState.updateDiagonalNeighbors(world, pos, newFlag);
        }

        world.onBlockStateChange(pos, originalState, newWorldState);

        return true;
        // } // Sponge - unnecessary formatting

    }

    /**
     * This will schedule the block changes to occur at the beginning of the next game tick,
     * or at the end of the current tick if we're already in the middle of a game tick.
     *
     * <p>Possible advanced solution may include:
     * - Determining if the Server's PhaseTracker is in the middle of a Server tick,
     *   - If in the middle of the Server tick, check if any worlds are ticking,
     *      - If a world is ticking, check if the desired world has ticked, is ticking, or is going
     *        to be ticked. If it's already ticked, schedule the block change at the end of the server
     *        tick. If the world is ticking, schedule to change the block at the end of the world tick,
     *        and if the world is going to be ticked, schedule the change to occur at the beginning
     *        of the world tick.
     * This solution would allow for "best" simulation in accordance to scheduled block updates
     * as a byproduct of the block change, as well as possible entity changes in the world etc. etc.
     * This also allows for most "time appropriate" changes to occur when they would otherwise
     * occur if the original proposed change were on the main thread.
     * </p>
     *
     * @param defensiveCopy
     * @param trackedWorld
     * @param pos
     * @param newState
     * @param flag
     */
    private void proposeScheduledBlockChange(final PhaseContext<?> defensiveCopy, final TrackedWorldBridge trackedWorld, final BlockPos pos, final net.minecraft.block.BlockState newState, final BlockChangeFlag flag) throws InterruptedException {
        trackedWorld.bridge$getScheduledBlockChangeList().put(new ScheduledBlockChange(defensiveCopy, pos, newState, flag));
    }


    /**
     * This is the replacement of {@link net.minecraft.world.World#addEntity(net.minecraft.entity.Entity)}
     * where it captures into phases. The causes and relations are processed by the phases.
     *
     * The difference between {@link #spawnEntityWithCause(World, Entity)} is that it bypasses
     * any phases and directly throws a spawn entity event.
     *
     * @param world The world
     * @param entity The entity
     * @return True if the entity spawn was successful
     */
    @SuppressWarnings("rawtypes")
    public boolean spawnEntity(final ServerWorld world, final net.minecraft.entity.Entity entity) {
        checkNotNull(entity, "Entity cannot be null!");
        // Sponge Start - Check if restoring blocks, don't want entities to be spawned
        if (entity instanceof ItemEntity && SpongeImplHooks.isRestoringBlocks(world)) {
            return false;
        }
        // Sponge End
        if (entity.removed) {
            // Vanilla raw logger usage, Sponge uses the accessor, same thing.
            // LOGGER.warn("Tried to add entity {} but it was marked as removed already", (Object)EntityType.getKey(entityIn.getType()));
            ServerWorldAccessor.accessor$LOGGER().warn("Tried to add entity {} but it was marked as removed already", EntityType.getKey(entity.getType()));
            return false;
        }

        // Sponge Start - handle construction phases
        if (((EntityBridge) entity).bridge$isConstructing()) {
            ((EntityBridge) entity).bridge$fireConstructors();
        }

        final PhaseContext<?> context = this.stack.peek();
        final IPhaseState<?> phaseState = context.state;
        final boolean isForced = entity.forceSpawn || entity instanceof PlayerEntity;

        // Certain phases disallow entity spawns (such as block restoration)
        if (!isForced && !phaseState.doesAllowEntitySpawns()) {
            return false;
        }
        // Sponge End
        // } else if (this.hasDuplicateEntity(entityIn)) { // Sponge can't use the direct method, it's private.
        if (((ServerWorldAccessor) world).accessor$hasDuplicateEntity(entity)) {
            return false;
        }
        // Forge needs their event here for EntityJoinWorldEvent
        // if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.EntityJoinWorldEvent(entity, this)))
        //    return false;
        if (SpongeImplHooks.canEntityJoinWorld(entity, world)) {
            return false;
        }
        // Forge End
        final IChunk ichunk = world.getChunk(MathHelper.floor(entity.posX / 16.0D), MathHelper.floor(entity.posZ / 16.0D), ChunkStatus.FULL, entity.forceSpawn);
        if (!(ichunk instanceof Chunk)) {
            return false;
        }


        // Sponge start - check for vanilla owner
        if (entity instanceof ThrowableEntity) {
            final ThrowableEntity throwable = (ThrowableEntity) entity;
            final LivingEntity thrower = throwable.getThrower();
            if (thrower != null) {
                final User user;
                if (thrower instanceof OwnershipTrackedBridge) {
                    user = ((OwnershipTrackedBridge) thrower).tracked$getOwnerReference().orElse(null);
                } else {
                    user = (User) thrower;
                }
                if (user != null) {
                    context.owner = user;
                    if (entity instanceof OwnershipTrackedBridge) {
                        ((OwnershipTrackedBridge) entity).tracked$setOwnerReference(user);
                    } else {
                        ((Entity) entity).offer(Keys.CREATOR, user.getUniqueId());
                    }
                }
            }
        }
        // Sponge end
        // Sponge Start
        // First, check if the owning world is a remote world. Then check if the spawn is forced.
        // Finally, if all checks are true, then let the phase process the entity spawn. Most phases
        // will not actively capture entity spawns, but will still throw events for them. Some phases
        // capture all entities until the phase is marked for completion.
        if (!isForced) {
            if (ShouldFire.SPAWN_ENTITY_EVENT
                || (ShouldFire.CHANGE_BLOCK_EVENT
                    // This bottom part of the if is due to needing to be able to capture block entity spawns
                    // while block events are being listened to
                    && ((IPhaseState) phaseState).doesBulkBlockCapture(context)
                    && ((IPhaseState) phaseState).tracksBlockSpecificDrops(context)
                    && context.getCaptureBlockPos().getPos().isPresent())) {
                try {
                    return ((IPhaseState) phaseState).spawnEntityOrCapture(context, (Entity) entity);
                } catch (final Exception | NoClassDefFoundError e) {
                    // Just in case something really happened, we should print a nice exception for people to
                    // paste us
                    PhasePrinter.printExceptionSpawningEntity(this, context, e);
                    return false;
                }
            }
        }
        final net.minecraft.entity.Entity customEntity = SpongeImplHooks.getCustomEntityIfItem(entity);
        final net.minecraft.entity.Entity finalEntityToSpawn = customEntity == null ? entity : customEntity;
        // Sponge end - continue on with the checks.
        ichunk.addEntity(entity);
        // world.onEntityAdded(entity); // Vanilla has a privaate method, Forge makes it public, accessors go!
        ((ServerWorldAccessor) world).accessor$onEntityAdded(finalEntityToSpawn);
        return true;

    }

    /**
     * The core implementation of {@link World#spawnEntity(Entity)} that
     * bypasses any sort of cause tracking and throws an event directly
     *
     * @param world The world
     * @param entity The entity
     * @return True if entity was spawned, false if not
     */
    public boolean spawnEntityWithCause(final World<?> world, final Entity entity) {
        checkNotNull(entity, "Entity cannot be null!");

        // Sponge Start - handle construction phases
        if (((EntityBridge) entity).bridge$isConstructing()) {
            ((EntityBridge) entity).bridge$fireConstructors();
        }

        final net.minecraft.entity.Entity minecraftEntity = (net.minecraft.entity.Entity) entity;
        final ServerWorld worldServer = (ServerWorld) world;
        // Sponge End - continue with vanilla mechanics

        final IChunk ichunk = worldServer.getChunk(MathHelper.floor(minecraftEntity.posX / 16.0D), MathHelper.floor(minecraftEntity.posZ / 16.0D), ChunkStatus.FULL, minecraftEntity.forceSpawn);
        if (!(ichunk instanceof Chunk)) {
            return false;
        }

        // Sponge Start - throw an event
        final List<Entity> entities = new ArrayList<>(1); // We need to use an arraylist so that filtering will work.
        entities.add(entity);

        final SpawnEntityEvent.Custom
            event =
            SpongeEventFactory.createSpawnEntityEventCustom(Sponge.getCauseStackManager().getCurrentCause(), entities);
        SpongeImpl.postEvent(event);
        if (entity instanceof PlayerEntity || !event.isCancelled()) {
            EntityUtil.processEntitySpawn(entity, Optional::empty);
        }
        // Sponge end

        return true;
    }

    /**
     * Validates the {@link Entity} being spawned is being spawned on the main server
     * thread, if it is available. If the entity is NOT being spawned on the main server thread,
     * well..... a mod (or plugin) is attempting to spawn an entity to the world
     * <b>off thread</b>. The problem with doing this is that the PhaseTracker is
     * <b>not</b> thread safe, and capturing entities off thread is always bad.
     *
     * @param entity The entity to spawn
     * @return True if the entity spawn is on the main thread.
     */
    public static boolean isEntitySpawnInvalid(final Entity entity) {
        if (Sponge.isServerAvailable() && (Sponge.getServer().onMainThread() || SpongeImpl.getServer().isServerStopped())) {
            return false;
        }
        PhasePrinter.printAsyncEntitySpawn(entity);
        return true;
    }

    public void ensureEmpty() {
        if (!this.stack.isEmpty()) {
            PhasePrinter.printNonEmptyStack(this.stack);

            while (!this.stack.isEmpty()) {
                this.getCurrentContext().close();
            }
        }
    }
    private final IdentityHashMap<IPhaseState<?>, ArrayDeque<? extends PhaseContext<?>>> stateContextPool = new IdentityHashMap<>();

    public <C extends PhaseContext<C>> ArrayDeque<C> getContextPoolFor(final PooledPhaseState<? extends C> state) {
        return (ArrayDeque<C>) this.stateContextPool.computeIfAbsent(state, (newState) -> new ArrayDeque<>());
    }
}
