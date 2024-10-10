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


import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.LevelChunk;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKey;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.applaunch.config.common.PhaseTrackerCategory;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;
import org.spongepowered.common.event.cause.entity.SpongeSpawnTypes;
import org.spongepowered.common.event.tracking.phase.general.GeneralPhase;
import org.spongepowered.common.event.tracking.phase.tick.TickPhase;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.Preconditions;
import org.spongepowered.common.util.PrettyPrinter;
import org.spongepowered.common.util.ThreadUtil;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The core state machine of Sponge. Acts a as proxy between various engine objects by processing actions through
 * various {@link IPhaseState}s.
 */
@SuppressWarnings("unchecked")
@DefaultQualifier(value = NonNull.class)
public final class PhaseTracker implements CauseStackManager {

    public static final PhaseTracker CLIENT = new PhaseTracker();
    public static final PhaseTracker SERVER = new PhaseTracker();
    public static final Logger LOGGER = LogManager.getLogger(PhaseTracker.class);
    static final CopyOnWriteArrayList<net.minecraft.world.entity.Entity> ASYNC_CAPTURED_ENTITIES = new CopyOnWriteArrayList<>();
    private static final Map<Thread, PhaseTracker> SPINOFF_TRACKERS = new MapMaker().weakKeys().concurrencyLevel(8).makeMap();
    private static final boolean DEBUG_CAUSE_FRAMES = Boolean.parseBoolean(System.getProperty("sponge.debugcauseframes", "false"));
    private static final String INITIAL_POOL_SIZE_PROPERTY = "sponge.cause.initialFramePoolSize";
    private static final String MAX_POOL_SIZE_PROPERTY = "sponge.cause.maxFramePoolSize";
    private static final int INITIAL_POOL_SIZE;
    private static final int MAX_POOL_SIZE;

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

    public static CauseStackManager getCauseStackManager() {
        return PhaseTracker.getInstance();
    }

    public static Block validateBlockForNeighborNotification(final ServerLevel worldServer, final BlockPos pos, @Nullable Block blockIn,
        final BlockPos otherPos, final LevelChunk chunk) {
        if (blockIn == null) {
            // If the block is null, check with the PhaseState to see if it can perform a safe way
            final PhaseContext<?> currentContext = PhaseTracker.getInstance().getPhaseContext();
            final PhaseTrackerCategory trackerConfig = SpongeConfigs.getCommon().get().phaseTracker;

            if (currentContext.state == TickPhase.Tick.TILE_ENTITY) {
                // Try to save ourselves
                final @Nullable BlockEntity source = (BlockEntity) currentContext.getSource();

                final @Nullable BlockEntityType<?> type = Optional.ofNullable(source)
                                                   .map(BlockEntity::getType)
                                                   .orElse(null);
                if (type != null) {
                    @Nullable ResourceLocation id = BlockEntityType.getKey(type);
                    if (id == null) {
                        id = ResourceLocation.parse(source.getClass().getCanonicalName());
                    }
                    final Map<String, Boolean> autoFixedTiles = trackerConfig.autoFixNullSourceBlockProvidingBlockEntities;
                    final boolean contained = autoFixedTiles.containsKey(type.toString());
                    // If we didn't map the tile entity yet, we should apply the mapping
                    // based on whether the source is the same as the TileEntity.
                    if (!contained) {
                        autoFixedTiles.put(id.toString(), pos.equals(source.getBlockPos()));
                    }
                    final boolean useTile = contained && autoFixedTiles.get(id.toString());
                    if (useTile) {
                        blockIn = source.getBlockState().getBlock();
                    } else {
                        blockIn = (pos.getX() >> 4 == chunk.getPos().x && pos.getZ() >> 4 == chunk.getPos().z)
                                      ? chunk.getBlockState(pos).getBlock()
                                      : worldServer.getBlockState(pos).getBlock();
                    }
                    if (!contained && trackerConfig.reportNullSourceBlocksOnNeighborNotifications) {
                        PhasePrinter.printNullSourceBlockWithTile(pos, blockIn, otherPos, id, useTile, new NullPointerException("Null Source Block For TileEntity Neighbor Notification"));
                    }
                } else {
                    blockIn = (pos.getX() >> 4 == chunk.getPos().x && pos.getZ() >> 4 == chunk.getPos().z)
                                  ? chunk.getBlockState(pos).getBlock()
                                  : worldServer.getBlockState(pos).getBlock();
                    if (trackerConfig.reportNullSourceBlocksOnNeighborNotifications) {
                        PhasePrinter.printNullSourceBlockNeighborNotificationWithNoTileSource(pos, blockIn, otherPos,
                            new NullPointerException("Null Source Block For Neighbor Notification"));
                    }
                }

            } else {
                blockIn = (pos.getX() >> 4 == chunk.getPos().x && pos.getZ() >> 4 == chunk.getPos().z)
                              ? chunk.getBlockState(pos).getBlock()
                              : worldServer.getBlockState(pos).getBlock();
                if (trackerConfig.reportNullSourceBlocksOnNeighborNotifications) {
                    PhasePrinter.printNullSourceForBlock(worldServer, pos, blockIn, otherPos, new NullPointerException("Null Source Block For Neighbor Notification"));
                }
            }
        }
        return blockIn;
    }

    static {
        int initialPoolSize = 50;
        int maxPoolSize = 100;
        try {
            initialPoolSize = Integer.parseInt(System.getProperty(PhaseTracker.INITIAL_POOL_SIZE_PROPERTY, "50"));
        } catch (final NumberFormatException ex) {
            SpongeCommon.logger().warn("{} must be an integer, was set to {}. Defaulting to 50.",
                PhaseTracker.INITIAL_POOL_SIZE_PROPERTY,
                System.getProperty(PhaseTracker.INITIAL_POOL_SIZE_PROPERTY));
        }
        try {
            maxPoolSize = Integer.parseInt(System.getProperty(PhaseTracker.MAX_POOL_SIZE_PROPERTY, "100"));
        } catch (final NumberFormatException ex) {
            SpongeCommon.logger().warn("{} must be an integer, was set to {}. Defaulting to 100.",
                PhaseTracker.MAX_POOL_SIZE_PROPERTY,
                System.getProperty(PhaseTracker.MAX_POOL_SIZE_PROPERTY));
        }
        MAX_POOL_SIZE = Math.max(0, maxPoolSize);
        INITIAL_POOL_SIZE = Math.max(0, Math.min(PhaseTracker.MAX_POOL_SIZE, initialPoolSize));
    }

    private final Deque<Object> cause = Queues.newArrayDeque();
    // Frames in use
    private final Deque<SpongeCauseStackFrame> frames = Queues.newArrayDeque();
    // Frames not currently in use
    private final Deque<SpongeCauseStackFrame> framePool = new ArrayDeque<>(PhaseTracker.MAX_POOL_SIZE);
    private final Map<EventContextKey<?>, Object> ctx = Maps.newHashMap();
    private int min_depth = 0;
    private int[] duplicateCauses = new int[100];
    private @Nullable Cause cached_cause;
    private @Nullable EventContext cached_ctx;
    private final AtomicBoolean pendingProviders = new AtomicBoolean(false);
    private @Nullable WeakReference<Thread> sidedThread;
    private boolean hasRun = false;
    /*
     * Specifically a Deque because we need to replicate
     * the stack iteration from the bottom of the stack
     * to the top when pushing frames.
     */
    private final Deque<PhaseContext<?>> phaseContextProviders = new ArrayDeque<>();
    final PhaseStack stack = new PhaseStack();


    PhaseTracker() {
        for (int i = 0; i < PhaseTracker.INITIAL_POOL_SIZE; i++) {
            this.framePool.push(new SpongeCauseStackFrame(this));
        }
    }

    public void init() {
        if (this != PhaseTracker.SERVER) {
            return;
        }
        if (this.hasRun) {
            return;
        }
        this.hasRun = true;
        final Task task = Task.builder()
                .interval(Ticks.single())
                .execute(() -> {
                    if (PhaseTracker.ASYNC_CAPTURED_ENTITIES.isEmpty()) {
                        return;
                    }

                    final List<Entity> entities = new ArrayList<>(PhaseTracker.ASYNC_CAPTURED_ENTITIES);
                    PhaseTracker.ASYNC_CAPTURED_ENTITIES.removeAll(entities);
                    try (final StackFrame frame = this.pushCauseFrame()) {
                        // We are forcing the spawn, as we can't throw the proper event at the proper time, so
                        // we'll just mark it as "forced".
                        frame.addContext(EventContextKeys.SPAWN_TYPE, SpongeSpawnTypes.FORCED);
                        for (final Entity entity : entities) {
                            // At this point, we don't care what the causes are...
                            entity.getCommandSenderWorld().addFreshEntity(entity);
                        }
                    }

                })
                .plugin(Launch.instance().commonPlugin())
                .build();
        Sponge.server().scheduler().submit(task, "Sponge Async To Sync Entity Spawn Task");
    }

    public void setThread(final @Nullable Thread thread) throws IllegalAccessException {
        if ((this == PhaseTracker.SERVER || this == PhaseTracker.CLIENT) && thread == null) {
            this.sidedThread = new WeakReference<>(null);
            return;
        }

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

    public boolean onSidedThread() {
        return Thread.currentThread() == this.getSidedThread();
    }

    // ----------------- SIMPLE GETTERS --------------------------------------

    public IPhaseState<?> getCurrentState() {
        if (Thread.currentThread() != this.getSidedThread()) {
            throw new UnsupportedOperationException("Cannot access the PhaseTracker off-thread, please use the respective PhaseTracker for their proper thread.");
        }
        return this.stack.peekState();
    }

    public PhaseContext<?> getPhaseContext() {
        if (Thread.currentThread() != this.getSidedThread()) {
            throw new UnsupportedOperationException("Cannot access the PhaseTracker off-thread, please use the respective PhaseTracker for their proper thread.");
        }
        return this.stack.peekContext();
    }

    public @Nullable Thread getSidedThread() {
        return this.sidedThread != null ? this.sidedThread.get() : null;
    }

    // ----------------- STATE ACCESS ----------------------------------

    void switchToPhase(final IPhaseState<?> state, final PhaseContext<?> phaseContext) {
        if (phaseContext.createdTracker != this && Thread.currentThread() != this.getSidedThread()) {
            // lol no, report the block change properly
            new PrettyPrinter(60).add("Illegal Async PhaseTracker Access").centre().hr()
                    .addWrapped(PhasePrinter.ASYNC_TRACKER_ACCESS)
                    .add()
                    .add(new Exception("Async Block Change Detected"))
                    .log(SpongeCommon.logger(), Level.ERROR);
            // Maybe? I don't think this is wise.
            return;
        }
        Objects.requireNonNull(state, "State cannot be null!");
        Objects.requireNonNull(phaseContext, "PhaseContext cannot be null!");
        Preconditions.checkArgument(phaseContext.isComplete(), "PhaseContext must be complete!");
        if (this == PhaseTracker.SERVER && SpongeConfigs.getCommon().get().phaseTracker.verbose) {
            if (this.stack.size() > 6) {
                if (this.stack.checkForRunaways(state, phaseContext)) {
                    PhasePrinter.printRunawayPhase(this.stack, state, phaseContext);
                }

            }
        }

        if (phaseContext.shouldProvideModifiers()) {
            this.registerPhaseContextProvider(phaseContext);
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
                .log(SpongeCommon.logger(), Level.ERROR);
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

        if (SpongeConfigs.getCommon().get().phaseTracker.verbose) {
            if (this.stack.checkForRunaways(GeneralPhase.Post.UNWINDING, null)) {
                // This printing is to detect possibilities of a phase not being cleared properly
                // and resulting in a "runaway" phase state accumulation.
                PhasePrinter.printRunAwayPhaseCompletion(this.stack, state);
            }
        }

        final boolean hasCaptures = currentContext.hasCaptures();
        try (final @Nullable UnwindingPhaseContext unwinding = UnwindingPhaseContext.unwind(currentContext, hasCaptures)) {
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

    }

    private void checkPhaseContextProcessed(final IPhaseState<?> state, final PhaseContext<?> context) {
        if (!SpongeConfigs.getCommon().get().phaseTracker.verbose && PhasePrinter.printedExceptionsForUnprocessedState.contains(state)) {
            return;
        }

        if (context.notAllCapturesProcessed()) {
            PhasePrinter.printUnprocessedPhaseContextObjects(this.stack, state, context);
            PhasePrinter.printedExceptionsForUnprocessedState.add(state);

        }
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

    public void ensureEmpty() {
        if (!this.stack.isEmpty()) {
            PhasePrinter.printNonEmptyStack(this.stack);

            while (!this.stack.isEmpty()) {
                this.getPhaseContext().close();
            }
        }
    }

    private final IdentityHashMap<IPhaseState<?>, ArrayDeque<? extends PhaseContext<?>>> stateContextPool = new IdentityHashMap<>();

    public <C extends PhaseContext<C>> ArrayDeque<C> getContextPoolFor(final PooledPhaseState<? extends C> state) {
        return (ArrayDeque<C>) this.stateContextPool.computeIfAbsent(state, (newState) -> new ArrayDeque<>());
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final PhaseTracker that = (PhaseTracker) o;
        return this.hasRun == that.hasRun
                && (this.sidedThread != null && that.sidedThread != null && this.sidedThread.equals(that.sidedThread))
                || (this.sidedThread == null && that.sidedThread == null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.sidedThread);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", PhaseTracker.class.getSimpleName() + "[", "]")
                .add("sidedThread=" + this.sidedThread)
                .add("hasRun=" + this.hasRun)
                .toString();
    }

    @Override
    public Cause currentCause() {
        this.enforceMainThread();
        if (this.cached_cause == null || this.cached_ctx == null) {
            if (this.cause.isEmpty()) {
                this.cached_cause = Cause.of(this.currentContext(), SpongeCommon.game());
            } else {
                this.cached_cause = Cause.of(this.currentContext(), this.cause);
            }
        }
        return this.cached_cause;
    }

    @Override
    public EventContext currentContext() {
        this.enforceMainThread();
        if (this.cached_ctx == null) {
            this.cached_ctx = EventContext.of(this.ctx);
        }
        return this.cached_ctx;
    }

    @Override
    public CauseStackManager pushCause(final Object obj) {
        Objects.requireNonNull(obj, "obj");
        this.enforceMainThread();
        this.cached_cause = null;
        if (this.cause.peek() == obj) {
            // We don't want to be pushing duplicate objects
            // to the root and secondary entry of the cause.
            // This avoids some odd corner cases of the phase tracking system pushing
            // objects without being able to definitively say if the object is already pushed
            // without generating cause frames forcibly.
            // BUT, we do want to at least mark the index of the duplicated object for later popping (if some consumer is doing manual push and pops)
            final int dupedIndex = this.cause.size();
            if (this.duplicateCauses.length <= dupedIndex) {
                // Make sure that we have enough space. If not, increase by 50%
                this.duplicateCauses = Arrays.copyOf(this.duplicateCauses, (int) (dupedIndex * 1.5));
            }
            // Increase the value by 1 since we've obviously reached a new duplicate. This is to allow for
            // additional duplicates to be "popped" with proper indexing.
            this.duplicateCauses[dupedIndex] = this.duplicateCauses[dupedIndex] + 1;
            return this;
        }
        this.cause.push(obj);
        return this;
    }

    @Override
    public Object popCause() {
        this.enforceMainThread();
        final int size = this.cause.size();
        // First, check for duplicate causes. If there are duplicates,
        // we can artificially "pop" by just peeking.
        final int dupeCause = this.duplicateCauses[size];
        if (dupeCause > 0) {
            // Make sure to just decrement the duplicate causes.
            this.duplicateCauses[size] = dupeCause - 1;
            return Objects.requireNonNull(this.cause.peek());
        }
        if (size <= this.min_depth) {
            throw new IllegalStateException("Cause stack corruption, tried to pop more objects off than were pushed since last frame (Size was "
                                                + size + " but mid depth is " + this.min_depth + ")");
        }
        this.cached_cause = null;
        return this.cause.pop();
    }

    @Override
    public void popCauses(final int n) {
        this.enforceMainThread();
        for (int i = 0; i < n; i++) {
            this.popCause();
        }
    }

    @Override
    public Object peekCause() {
        this.enforceMainThread();
        return this.cause.peek();
    }

    @Override
    public StackFrame pushCauseFrame() {
        this.enforceMainThread();
        // Ensure duplicate causes will be correctly sized.
        final int size = this.cause.size();
        if (this.duplicateCauses.length <= size) {
            this.duplicateCauses = Arrays.copyOf(this.duplicateCauses, (int) (size * 1.5));
        }

        final SpongeCauseStackFrame frame;
        if (this.framePool.isEmpty()) {
            frame = new SpongeCauseStackFrame(this).set(this.min_depth, this.duplicateCauses[size]);
        } else {
            frame = this.framePool.pop();

            // Just in case we didn't catch a corrupted frame, clear it to ensure that we have
            // a clean slate.
            frame.clear();
            frame.old_min_depth = this.min_depth;
            frame.lastCauseSize = this.duplicateCauses[size];
        }

        this.frames.push(frame);
        this.min_depth = size;
        if (PhaseTracker.DEBUG_CAUSE_FRAMES) {
            // Attach an exception to the frame so that if there is any frame
            // corruption we can print out the stack trace of when the frames
            // were created.
            frame.stackDebug = new Exception();
        }
        return frame;
    }

    @Override
    public void popCauseFrame(final StackFrame oldFrame) {
        Objects.requireNonNull(oldFrame, "oldFrame");
        this.enforceMainThread();
        final @Nullable SpongeCauseStackFrame frame = this.frames.peek();
        if (frame != oldFrame) {
            // If the given frame is not the top frame then some form of
            // corruption of the stack has occurred and we do our best to correct
            // it.

            // If the target frame is still in the stack then we can pop frames
            // off the stack until we reach it, otherwise we have no choice but
            // to simply throw an error.
            int offset = -1;
            int i = 0;
            for (final SpongeCauseStackFrame f : this.frames) {
                if (f == oldFrame) {
                    offset = i;
                    break;
                }
                i++;
            }
            if (!PhaseTracker.DEBUG_CAUSE_FRAMES && offset == -1) {
                // if we're not debugging the cause frames then throw an error
                // immediately otherwise let the pretty printer output the frame
                // that was erroneously popped.
                throw new IllegalStateException("Cause Stack Frame Corruption! Attempted to pop a frame that was not on the stack.");
            }
            final PrettyPrinter printer = new PrettyPrinter(100).add("Cause Stack Frame Corruption!").centre().hr()
                                              .add("Found %d frames left on the stack. Clearing them all.", new Object[]{offset + 1});
            if (!PhaseTracker.DEBUG_CAUSE_FRAMES) {
                printer.add()
                    .add("Please add -Dsponge.debugcauseframes=true to your startup flags to enable further debugging output.");
                SpongeCommon.logger().warn("  Add -Dsponge.debugcauseframes to your startup flags to enable further debugging output.");
            } else {
                printer.add()
                    .add("Attempting to pop frame:")
                    .add(frame.stackDebug)
                    .add()
                    .add("Frames being popped are:")
                    .add(((SpongeCauseStackFrame) oldFrame).stackDebug);
            }

            while (offset >= 0) {
                final @Nullable SpongeCauseStackFrame f = this.frames.peek();
                if (PhaseTracker.DEBUG_CAUSE_FRAMES && offset > 0) {
                    printer.add("   Stack frame in position %d :", new Object[]{offset});
                    printer.add(f.stackDebug);
                }
                this.popCauseFrame(f);
                offset--;
            }
            printer.trace(System.err, SpongeCommon.logger(), Level.ERROR);
            if (offset == -1) {
                // Popping a frame that was not on the stack is not recoverable
                // so we throw an exception.
                throw new IllegalStateException("Cause Stack Frame Corruption! Attempted to pop a frame that was not on the stack.");
            }
            return;
        }
        this.frames.pop();

        // Remove new values
        for (final Map.Entry<EventContextKey<?>, Object> entry : frame.getOriginalContextDelta().entrySet()) {
            this.cached_ctx = null;
            this.cached_cause = null;
            if (entry.getValue() == null) { // wasn't present before, remove
                this.ctx.remove(entry.getKey());
            } else { // was there, replace
                this.ctx.put(entry.getKey(), entry.getValue());
            }
        }

        // If there were any objects left on the stack then we pop them off
        while (this.cause.size() > this.min_depth) {
            final int index = this.cause.size();

            // Then, only pop the potential duplicate causes (if any) if and only if
            // there was a duplicate cause pushed prior to the frame being popped.
            if (this.duplicateCauses.length > index) {
                // At this point, we now need to "clean" the duplicate causes array of duplicates
                // to avoid potentially pruning earlier frame's potentially duplicate causes.
                // And of course, reset the number of duplicates in the entry.
                this.duplicateCauses[index] = 0;
            }
            this.cause.pop();

            // and clear the cached causes
            this.cached_cause = null;
        }
        this.min_depth = frame.old_min_depth;
        final int size = this.cause.size();
        if (this.duplicateCauses.length > size) {
            // Then set the last cause index to whatever the size of the entry was at the time.
            this.duplicateCauses[size] = frame.lastCauseSize;
        }

        // finally, return the frame to the pool
        if (this.framePool.size() < PhaseTracker.MAX_POOL_SIZE) {
            // cache it, but also call clear so we remove references to
            // other objects that may go out of scope
            frame.clear();
            this.framePool.push(frame);
        }
    }

    @Override
    public <T> CauseStackManager addContext(final EventContextKey<T> key, final T value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        this.enforceMainThread();
        this.cached_ctx = null;
        this.cached_cause = null;
        final @Nullable Object existing = this.ctx.put(key, value);
        if (!this.frames.isEmpty()) {
            this.frames.peek().storeOriginalContext(key, existing);
        }
        return this;
    }

    @Override
    public <T> Optional<T> context(final EventContextKey<T> key) {
        Objects.requireNonNull(key, "key");
        this.enforceMainThread();
        return Optional.ofNullable((T) this.ctx.get(key));
    }

    @Override
    public <T> Optional<T> removeContext(final EventContextKey<T> key) {
        Objects.requireNonNull(key, "key");
        this.enforceMainThread();
        this.cached_ctx = null;
        this.cached_cause = null;
        final Object existing = this.ctx.remove(key);
        if (!this.frames.isEmpty()) {
            this.frames.peek().storeOriginalContext(key, existing);
        }
        return Optional.ofNullable((T) existing);
    }

    private void enforceMainThread() {
        // On clients, this may not be available immediately, we can't bomb out that early.
        if (Thread.currentThread() != this.getSidedThread()) {
            throw new IllegalStateException(String.format(
                "CauseStackManager called from off main thread (current='%s', expected='%s')!",
                ThreadUtil.getDescription(Thread.currentThread()),
                ThreadUtil.getDescription(SpongeCommon.server().getRunningThread())
            ));
        }
        this.checkProviders();
    }

    @SuppressWarnings("rawtypes")
    private void checkProviders() {
        // Seriously, ok so, uh...
        if (!this.pendingProviders.compareAndSet(true, false)) {
            return; // we've done our work already
        }
        // Then, we want to inversely iterate the stack (from bottom to top)
        // to properly mimic as though the frames were created at the time of the
        // phase switches. It does not help the debugging of cause frames
        // except for this method call-point.
        for (final Iterator<PhaseContext<@NonNull ?>> iterator = this.phaseContextProviders.descendingIterator(); iterator.hasNext(); ) {
            final PhaseContext<@NonNull ?> context = iterator.next();
            final StackFrame frame = this.pushCauseFrame(); // these should auto close
            context.getFrameModifier().accept(frame); // The frame will be auto closed by the phase context
        }
        // Clear the list since everything is now loaded.
        // PhaseStates will handle automatically closing their frames
        // and then any new phase states that get entered can still be lazily loaded afterwards, while
        // we take advantage of the already made modifications are being tracked by the stack manager
        this.phaseContextProviders.clear();
    }


    private void registerPhaseContextProvider(final PhaseContext<?> context) {
        Objects.requireNonNull(context.state.getFrameModifier(), "Consumer");
        // Reset our cached objects
        this.pendingProviders.compareAndSet(false, true); //I Reset the cache
        this.cached_cause = null; // Reset the cache
        this.cached_ctx = null; // Reset the cache
        // Since we cannot rely on the PhaseStack being tied to this stack of providers,
        // we have to make the tuple to tie the phase context to provide the consumer.
        this.phaseContextProviders.push(context);
    }

    void popFrameMutator(final PhaseContext<?> context) {
        final @Nullable PhaseContext<?> peek = this.phaseContextProviders.peek();
        if (peek == null) {
            return;
        }
        if (peek != context) {
            // there's an exception to be thrown or printed out at least, basically a copy of popFrame.
            System.err.println("oops. corrupted phase context providers!");
        }
        this.phaseContextProviders.pop();
        if (this.phaseContextProviders.isEmpty()) {
            // if we're empty, we don't need to bother with the context providers
            // because there's nothing to push.
            this.pendingProviders.compareAndSet(true, false);
        }

    }
}
