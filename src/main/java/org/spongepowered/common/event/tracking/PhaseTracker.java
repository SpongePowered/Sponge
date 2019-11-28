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
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntityType;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Tuple;
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
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.category.PhaseTrackerCategory;
import org.spongepowered.common.config.type.GlobalConfig;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.context.SpongeProxyBlockAccess;
import org.spongepowered.common.event.tracking.phase.general.GeneralPhase;
import org.spongepowered.common.event.tracking.phase.tick.NeighborNotificationContext;
import org.spongepowered.common.event.tracking.phase.tick.TickPhase;
import org.spongepowered.common.mixin.core.world.WorldServerAccessor;
import org.spongepowered.common.registry.type.event.SpawnTypeRegistryModule;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.world.SpongeBlockChangeFlag;
import org.spongepowered.common.world.SpongeLocatableBlockBuilder;
import org.spongepowered.common.world.WorldManager;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

/**
 * The core state machine of Sponge. Acts a as proxy between various engine objects by processing actions through
 * various {@link IPhaseState}s.
 */
@SuppressWarnings("unchecked")
public final class PhaseTracker {
    public static final PhaseTracker CLIENT = new PhaseTracker();
    public static final PhaseTracker SERVER = new PhaseTracker();


    public void init() {
        if (this != SERVER) {
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
                if (ASYNC_CAPTURED_ENTITIES.isEmpty()) {
                    return;
                }

                final List<net.minecraft.entity.Entity> entities = new ArrayList<>(ASYNC_CAPTURED_ENTITIES);
                ASYNC_CAPTURED_ENTITIES.removeAll(entities);
                try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                    // We are forcing the spawn, as we can't throw the proper event at the proper time, so
                    // we'll just mark it as "forced".
                    frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypeRegistryModule.FORCED);
                    for (net.minecraft.entity.Entity entity : entities) {
                        // At this point, we don't care what the causes are...
                        PhaseTracker.getInstance().spawnEntityWithCause((World) entity.func_130014_f_(), (Entity) entity);
                    }
                }

            })
            .submit(SpongeImpl.getPlugin());
    }

    @Nullable private Thread sidedThread;
    private boolean hasRun = false;


    @SuppressWarnings("ThrowableNotThrown")
    public void setThread(@Nullable final Thread thread) throws IllegalAccessException {
        final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        if ((stackTrace.length < 3)) {
            throw new IllegalAccessException("Cannot call directly to change thread.");
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

        this.sidedThread = thread;

    }



    @Nullable
    public Thread getSidedThread() {
        return this.sidedThread;
    }

    public static final String ASYNC_BLOCK_CHANGE_MESSAGE = "Sponge adapts the vanilla handling of block changes to power events and plugins "
                                                + "such that it follows the known fact that block changes MUST occur on the server "
                                                + "thread (even on clients, this exists as the InternalServer thread). It is NOT "
                                                + "possible to change this fact and must be reported to the offending mod for async "
                                                + "issues.";
    public static final String ASYNC_TRACKER_ACCESS = "Sponge adapts the vanilla handling of various processes, such as setting a block "
                                                      + "or spawning an entity. Sponge is designed around the concept that Minecraft is "
                                                      + "primarily performing these operations on the \"server thread\". Because of this "
                                                      + "Sponge is safeguarding common access to the PhaseTracker as the entrypoint for "
                                                      + "performing these sort of changes.";

    public static PhaseTracker getInstance() {
        return SERVER;
    }

    private static final CopyOnWriteArrayList<net.minecraft.entity.Entity> ASYNC_CAPTURED_ENTITIES = new CopyOnWriteArrayList<>();



    public static final BiConsumer<PrettyPrinter, PhaseContext<?>> CONTEXT_PRINTER = (printer, context) ->
        context.printCustom(printer, 4);

    private static final BiConsumer<PrettyPrinter, PhaseContext<?>> PHASE_PRINTER = (printer, context) -> {
        printer.add("  - Phase: %s", context.state);
        printer.add("    Context:");
        context.printCustom(printer, 4);
        context.printTrace(printer);
    };

    private final PhaseStack stack = new PhaseStack();

    private boolean hasPrintedEmptyOnce = false;
    private boolean hasPrintedAboutRunnawayPhases = false;
    private boolean hasPrintedAsyncEntities = false;
    private int printRunawayCount = 0;
    private final List<IPhaseState<?>> printedExceptionsForBlocks = new ArrayList<>();
    private final List<IPhaseState<?>> printedExceptionsForEntities = new ArrayList<>();
    private final List<Tuple<IPhaseState<?>, IPhaseState<?>>> completedIncorrectStates = new ArrayList<>();
    private final List<IPhaseState<?>> printedExceptionsForState = new ArrayList<>();
    private final Set<IPhaseState<?>> printedExceptionsForUnprocessedState = new HashSet<>();
    private final Set<IPhaseState<?>> printedExceptionForMaximumProcessDepth = new HashSet<>();
    private final ConcurrentHashMap<IPhaseState<?>, ArrayDeque<? extends PhaseContext<?>>> stateContextPool = new ConcurrentHashMap<>();

    // ----------------- STATE ACCESS ----------------------------------

    public <C extends PhaseContext<C>> ArrayDeque<C> createContextPool(final IPhaseState<C> state) {
        final ArrayDeque<C> pool = new ArrayDeque<>();
        this.stateContextPool.put(state, pool);
        return pool;
    }

    @SuppressWarnings("rawtypes")
    void switchToPhase(final IPhaseState<?> state, final PhaseContext<?> phaseContext) {
        if (!SpongeImplHooks.isMainThread()) {
            // lol no, report the block change properly
            new PrettyPrinter(60).add("Illegal Async PhaseTracker Access").centre().hr()
                .addWrapped(ASYNC_TRACKER_ACCESS)
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
                    this.printRunawayPhase(state, phaseContext);
                }

            }
        }

        if (Sponge.isServerAvailable() && ((IPhaseState) state).shouldProvideModifiers(phaseContext)) {
            SpongeImpl.getCauseStackManager().registerPhaseContextProvider(phaseContext);
        }
        this.stack.push(state, phaseContext);
    }

    @SuppressWarnings({"rawtypes", "unused", "try"})
    void completePhase(final IPhaseState<?> prevState) {
        if (!SpongeImplHooks.isMainThread()) {
            // lol no, report the block change properly
            new PrettyPrinter(60).add("Illegal Async PhaseTracker Access").centre().hr()
                .addWrapped(ASYNC_TRACKER_ACCESS)
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
            this.printEmptyStackOnCompletion(currentContext);
            return;
        }

        if (prevState != state) {
            this.printIncorrectPhaseCompletion(prevState, state);

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
                this.printRunnawayPhaseCompletion(state);
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
                this.printMessageWithCaughtException("Exception Exiting Phase", "Something happened when trying to unwind", state, currentContext, e);
            }
        } catch (final Exception e) {
            this.printMessageWithCaughtException("Exception Post Dispatching Phase", "Something happened when trying to post dispatch state", state,
                currentContext, e);
        }
        this.checkPhaseContextProcessed(state, currentContext);
        // If pop is called, the Deque will already throw an exception if there is no element
        // so it's an error properly handled.
        this.stack.pop();

        if (this.stack.isEmpty()) {
            for (final WorldServer world : WorldManager.getWorlds()) {
                final WorldServerBridge mixinWorld = (WorldServerBridge) world;
                if (mixinWorld.bridge$getProxyAccess().hasProxy()) {
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

    private void printRunnawayPhaseCompletion(final IPhaseState<?> state) {
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().isVerbose() && !this.hasPrintedAboutRunnawayPhases) {
            // Avoiding spam logs.
            return;
        }
        final PrettyPrinter printer = new PrettyPrinter(60);
        printer.add("Completing Phase").centre().hr();
        printer.addWrapped(60, "Detecting a runaway phase! Potentially a problem "
                               + "where something isn't completing a phase!!! Sponge will stop printing"
                               + "after three more times to avoid generating extra logs");
        printer.add();
        printer.addWrapped(60, "%s : %s", "Completing phase", state);
        printer.add(" Phases Remaining:");
        printPhaseStackWithException(this, printer, new Exception("RunawayPhase"));
        printer.trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().isVerbose() && this.printRunawayCount++ > 3) {
            this.hasPrintedAboutRunnawayPhases = true;
        }
    }

    public void generateVersionInfo(final PrettyPrinter printer) {
        for (final PluginContainer pluginContainer : SpongeImpl.getInternalPlugins()) {
            pluginContainer.getVersion().ifPresent(version ->
                    printer.add("%s : %s", pluginContainer.getName(), version)
            );
        }
    }

    private void printIncorrectPhaseCompletion(final IPhaseState<?> prevState, final IPhaseState<?> state) {
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().isVerbose() && !this.completedIncorrectStates.isEmpty()) {
            for (final Tuple<IPhaseState<?>, IPhaseState<?>> tuple : this.completedIncorrectStates) {
                if ((tuple.getFirst().equals(prevState)
                        && tuple.getSecond().equals(state))) {
                    // we've already printed once about the previous state and the current state
                    // being completed incorrectly. only print it once.
                    return;
                }
            }
        }

        final PrettyPrinter printer = new PrettyPrinter(60).add("Completing incorrect phase").centre().hr()
                .addWrapped("Sponge's tracking system is very dependent on knowing when"
                        + " a change to any world takes place, however, we are attempting"
                        + " to complete a \"phase\" other than the one we most recently entered."
                        + " This is an error usually on Sponge's part, so a report"
                        + " is required on the issue tracker on GitHub.").hr()
                .add("Expected to exit phase: %s", prevState)
                .add("But instead found phase: %s", state)
                .add("StackTrace:")
                .add(new Exception());
        printer.add(" Phases Remaining:");
        printPhaseStackWithException(this, printer, new Exception("Incorrect Phase Completion"));
        printer.trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().isVerbose()) {
            this.completedIncorrectStates.add(new Tuple<>(prevState, state));
        }
    }

    private void printEmptyStackOnCompletion(final PhaseContext<?> context) {
        if (this.hasPrintedEmptyOnce) {
            // We want to only mention it once that we are completing an
            // empty state, of course something is bound to break, but
            // we don't want to spam megabytes worth of log files just
            // because of it.
            return;
        }
        final PrettyPrinter printer = new PrettyPrinter(60).add("Unexpectedly Completing An Empty Stack").centre().hr()
                .addWrapped(60, "Sponge's tracking system is very dependent on knowing when"
                                + " a change to any world takes place, however, we have been told"
                                + " to complete a \"phase\" without having entered any phases."
                                + " This is an error usually on Sponge's part, so a report"
                                + " is required on the issue tracker on GitHub.").hr()
                .add("StackTrace:")
                .add(new Exception())
                .add("Phase being completed:");
        PHASE_PRINTER.accept(printer, context);
        printer.add();
        this.generateVersionInfo(printer);
        printer.trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().isVerbose()) {
            this.hasPrintedEmptyOnce = true;
        }
    }

    private void printRunawayPhase(final IPhaseState<?> state, final PhaseContext<?> context) {
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().isVerbose() && !this.hasPrintedAboutRunnawayPhases) {
            // Avoiding spam logs.
            return;
        }
        final PrettyPrinter printer = new PrettyPrinter(60);
        printer.add("Switching Phase").centre().hr();
        printer.addWrapped(60, "Detecting a runaway phase! Potentially a problem where something isn't completing a phase!!!");
        printer.add("  %s : %s", "Entering State", state);
        CONTEXT_PRINTER.accept(printer, context);
        printer.addWrapped(60, "%s :", "Phases remaining");
        printPhaseStackWithException(this, printer, new Exception("RunawayPhase"));
        printer.trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().isVerbose() && this.printRunawayCount++ > SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().getMaximumRunawayCount()) {
            this.hasPrintedAboutRunnawayPhases = true;
        }
    }

    public static void printNullSourceForBlock(final WorldServer worldServer, final BlockPos pos, final Block blockIn, final BlockPos otherPos,
        final NullPointerException e) {
        final PhaseTracker instance = PhaseTracker.getInstance();
        final PrettyPrinter printer = new PrettyPrinter(60).add("Null Source Block from Unknown Source!").centre().hr()
            .addWrapped("Hey, Sponge is saving the game from crashing or spamming because some source "
                        + "put up a \"null\" Block as it's source for sending out a neighbor notification. "
                        + "This is usually unsupported as the game will silently ignore some nulls by "
                        + "performing \"==\" checks instead of calling methods, potentially making an "
                        + "NPE. Because Sponge uses the source block to build information for tracking, "
                        + "Sponge has to save the game from crashing by reporting this issue. Because the "
                        + "source is unknown, it's recommended to report this issue to SpongeCommon's "
                        + "issue tracker on GitHub. Please provide the following information: ")
            .add()
            .add(" %s : %s", "Source position", pos)
            .add(" %s : %s", "World", ((World) worldServer).getName())
            .add(" %s : %s", "Source Block Recovered", blockIn)
            .add(" %s : %s", "Notified Position", otherPos).add();

        printPhaseStackWithException(instance, printer, e);
        printer
            .log(SpongeImpl.getLogger(), Level.WARN);
    }


    public static void printNullSourceBlockWithTile(
        final BlockPos pos, final Block blockIn, final BlockPos otherPos, final TileEntityType type, final boolean useTile,
        final NullPointerException e) {
        final PhaseTracker instance = PhaseTracker.getInstance();
        final PrettyPrinter printer = new PrettyPrinter(60).add("Null Source Block on TileEntity!").centre().hr()
            .addWrapped("Hey, Sponge is saving the game from crashing because a TileEntity "
                        + "is sending out a \'null\' Block as it's source (more likely) and "
                        + "attempting to perform a neighbor notification with it. Because "
                        + "this is guaranteed to lead to a crash or a spam of reports, "
                        + "Sponge is going ahead and fixing the issue. The offending Tile "
                        + "is " + type.getId())
            .add()
            .add("%s : %s", "Source position", pos)
            .add("%s : %s", "Source TileEntity", type)
            .add("%s : %s", "Recovered using TileEntity as Source", useTile)
            .add("%s : %s", "Source Block Recovered", blockIn)
            .add("%s : %s", "Notified Position", otherPos);
        printPhaseStackWithException(instance, printer, e);
        printer
            .log(SpongeImpl.getLogger(), Level.WARN);
    }

    public static void printNullSourceBlockNeighborNotificationWithNoTileSource(final BlockPos pos, final Block blockIn, final BlockPos otherPos,
        final NullPointerException e) {
        final PhaseTracker instance = PhaseTracker.getInstance();
        final PrettyPrinter printer = new PrettyPrinter(60).add("Null Source Block on TileEntity!").centre().hr()
            .addWrapped("Hey, Sponge is saving the game from crashing because a TileEntity "
                        + "is sending out a \'null\' Block as it's source (more likely) and "
                        + "attempting to perform a neighbor notification with it. Because "
                        + "this is guaranteed to lead to a crash or a spam of reports, "
                        + "Sponge is going ahead and fixing the issue. The offending Tile "
                        + "is unknown, so we don't have any way to configure a reporting for you")
            .add()
            .add("%s : %s", "Source position", pos)
            .add("%s : %s", "Source TileEntity", "UNKNOWN")
            .add("%s : %s", "Recovered using TileEntity as Source", "false")
            .add("%s : %s", "Source Block Recovered", blockIn)
            .add("%s : %s", "Notified Position", otherPos);
        printPhaseStackWithException(instance, printer, e);
        printer
            .log(SpongeImpl.getLogger(), Level.WARN);
    }

    public static void printPhaseStackWithException(final PhaseTracker instance, final PrettyPrinter printer, final Throwable e) {
        instance.stack.forEach(data -> PHASE_PRINTER.accept(printer, data));
        printer.add()
            .add(" %s :", "StackTrace")
            .add(e)
            .add();
        instance.generateVersionInfo(printer);
    }


    public void printMessageWithCaughtException(final String header, final String subHeader, @Nullable final Throwable e) {
        this.printMessageWithCaughtException(header, subHeader, this.getCurrentState(), this.getCurrentContext(), e);
    }

    private void printMessageWithCaughtException(final String header, final String subHeader, final IPhaseState<?> state, final PhaseContext<?> context, @Nullable final Throwable t) {
        final PrettyPrinter printer = new PrettyPrinter(60);
        printer.add(header).centre().hr()
                .add("%s %s", subHeader, state)
                .addWrapped(60, "%s :", "PhaseContext");
        CONTEXT_PRINTER.accept(printer, context);
        printer.addWrapped(60, "%s :", "Phases remaining");
        this.stack.forEach(data -> PHASE_PRINTER.accept(printer, data));
        if (t != null) {
            printer.add("Stacktrace:")
                    .add(t);
            if (t.getCause() != null) {
                printer.add(t.getCause());
            }
        }
        printer.add();
        this.generateVersionInfo(printer);
        printer.trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
    }

    public void printExceptionFromPhase(final Throwable e, final PhaseContext<?> context) {
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().isVerbose() && !this.printedExceptionsForState.isEmpty()) {
            for (final IPhaseState<?> iPhaseState : this.printedExceptionsForState) {
                if (context.state == iPhaseState) {
                    return;
                }
            }
        }
        final PrettyPrinter printer = new PrettyPrinter(60).add("Exception occurred during a PhaseState").centre().hr()
            .addWrapped("Sponge's tracking system makes a best effort to not throw exceptions randomly but sometimes it is inevitable. In most "
                    + "cases, something else triggered this exception and Sponge prevented a crash by catching it. The following stacktrace can be "
                    + "used to help pinpoint the cause.").hr()
            .add("The PhaseState having an exception: %s", context.state)
            .add("The PhaseContext:")
            ;
        printer
            .add(context.printCustom(printer, 4));
        printPhaseStackWithException(this, printer, e);

        printer.trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().isVerbose()) {
            this.printedExceptionsForState.add(context.state);
        }
    }

    private void checkPhaseContextProcessed(final IPhaseState<?> state, final PhaseContext<?> context) {
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().isVerbose() && this.printedExceptionsForUnprocessedState.contains(state)) {
            return;
        }

        if (context.notAllCapturesProcessed()) {
            this.printUnprocessedPhaseContextObjects(state, context);
            this.printedExceptionsForUnprocessedState.add(state);

        }
    }

    private void printUnprocessedPhaseContextObjects(final IPhaseState<?> state, final PhaseContext<?> context) {
        this.printMessageWithCaughtException("Failed to process all PhaseContext captured!",
                "During the processing of a phase, certain objects were captured in a PhaseContext. All of them should have been removed from the PhaseContext by this point",
                state, context, null);
    }

    private void printBlockTrackingException(final PhaseContext<?> phaseData, final IPhaseState<?> phaseState, final Throwable e) {
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().isVerbose() && !this.printedExceptionsForBlocks.isEmpty()) {
            if (this.printedExceptionsForBlocks.contains(phaseState)) {
                return;
            }
        }
        final PrettyPrinter printer = new PrettyPrinter(60).add("Exception attempting to capture a block change!").centre().hr();
        printPhasestack(phaseData, e, printer);
        printer.trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().isVerbose()) {
            this.printedExceptionsForBlocks.add(phaseState);
        }
    }

    private void printPhasestack(final PhaseContext<?> phaseData, final Throwable e, final PrettyPrinter printer) {
        printer.addWrapped(60, "%s :", "PhaseContext");
        CONTEXT_PRINTER.accept(printer, phaseData);
        printer.addWrapped(60, "%s :", "Phases remaining");
        this.stack.forEach(data -> PHASE_PRINTER.accept(printer, data));
        printer.add("Stacktrace:");
        printer.add(e);
    }

    private void printUnexpectedBlockChange(final WorldServerBridge mixinWorld, final BlockPos pos, final IBlockState currentState,
                                            final IBlockState newState) {
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().isVerbose()) {
            return;
        }
        new PrettyPrinter(60).add("Unexpected World Change Detected!").centre().hr()
            .add("Sponge's tracking system is very dependent on knowing when\n"
                 + "a change to any world takes place, however there are chances\n"
                 + "where Sponge does not know of changes that mods may perform.\n"
                 + "In cases like this, it is best to report to Sponge to get this\n"
                 + "change tracked correctly and accurately.").hr()
            .add()
            .add("%s : %s", "World", mixinWorld)
            .add("%s : %s", "Position", pos)
            .add("%s : %s", "Current State", currentState)
            .add("%s : %s", "New State", newState)
            .add()
            .add("StackTrace:")
            .add(new Exception())
            .trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
    }

    private void printExceptionSpawningEntity(final PhaseContext<?> context, final Throwable e) {
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().isVerbose() && !this.printedExceptionsForEntities.isEmpty()) {
            if (this.printedExceptionsForEntities.contains(context.state)) {
                return;
            }
        }
        final PrettyPrinter printer = new PrettyPrinter(60).add("Exception attempting to capture or spawn an Entity!").centre().hr();
        printPhasestack(context, e, printer);
        printer.log(SpongeImpl.getLogger(), Level.ERROR);
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().isVerbose()) {
            this.printedExceptionsForEntities.add(context.state);
        }
    }

    public static Block validateBlockForNeighborNotification(final WorldServer worldServer, final BlockPos pos, @Nullable Block blockIn,
        final BlockPos otherPos, final Chunk chunk) {
        if (blockIn == null) {
            // If the block is null, check with the PhaseState to see if it can perform a safe way
            final PhaseContext<?> currentContext = getInstance().getCurrentContext();
            final PhaseTrackerCategory trackerConfig = SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker();

            if (currentContext.state == TickPhase.Tick.TILE_ENTITY) {
                // Try to save ourselves
                final TileEntityType type = currentContext
                    .getSource(org.spongepowered.api.block.tileentity.TileEntity.class)
                    .map(org.spongepowered.api.block.tileentity.TileEntity::getType)
                    .orElse(null);
                if (type != null) {
                    final Map<String, Boolean> autoFixedTiles = trackerConfig.getAutoFixedTiles();
                    final boolean contained = autoFixedTiles.containsKey(type.getId());
                    // If we didn't map the tile entity yet, we should apply the mapping
                    // based on whether the source is the same as the TileEntity.
                    if (!contained) {
                        if (pos.equals(currentContext.getSource(TileEntity.class).get().func_174877_v())) {
                            autoFixedTiles.put(type.getId(), true);
                        } else {
                            autoFixedTiles.put(type.getId(), false);
                        }
                    }
                    final boolean useTile = contained && autoFixedTiles.get(type.getId());
                    if (useTile) {
                        blockIn = ((TileEntity) currentContext.getSource()).func_145838_q();
                    } else {
                        blockIn = (pos.func_177958_n() >> 4 == chunk.field_76635_g && pos.func_177952_p() >> 4 == chunk.field_76647_h)
                                  ? chunk.func_177435_g(pos).func_177230_c()
                                  : worldServer.func_180495_p(pos).func_177230_c();
                    }
                    if (!contained && trackerConfig.isReportNullSourceBlocks()) {
                        printNullSourceBlockWithTile(pos, blockIn, otherPos, type, useTile, new NullPointerException("Null Source Block For TileEntity Neighbor Notification"));
                    }
                } else {
                    blockIn = (pos.func_177958_n() >> 4 == chunk.field_76635_g && pos.func_177952_p() >> 4 == chunk.field_76647_h)
                              ? chunk.func_177435_g(pos).func_177230_c()
                              : worldServer.func_180495_p(pos).func_177230_c();
                    if (trackerConfig.isReportNullSourceBlocks()) {
                        printNullSourceBlockNeighborNotificationWithNoTileSource(pos, blockIn, otherPos,
                            new NullPointerException("Null Source Block For Neighbor Notification"));
                    }
                }

            } else {
                blockIn = (pos.func_177958_n() >> 4 == chunk.field_76635_g && pos.func_177952_p() >> 4 == chunk.field_76647_h)
                          ? chunk.func_177435_g(pos).func_177230_c()
                          : worldServer.func_180495_p(pos).func_177230_c();
                if (trackerConfig.isReportNullSourceBlocks()) {
                    printNullSourceForBlock(worldServer, pos, blockIn, otherPos, new NullPointerException("Null Source Block For Neighbor Notification"));
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
        this.stack.forEach(data -> PHASE_PRINTER.accept(printer, data));

        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        printer.print(new PrintStream(stream));

        return stream.toString();
    }

    // ----------------- SIMPLE GETTERS --------------------------------------

    public IPhaseState<?> getCurrentState() {
        return this.stack.peekState();
    }

    public PhaseContext<?> getCurrentContext() {
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
     */
    @SuppressWarnings("rawtypes")
    public void notifyBlockOfStateChange(final WorldServerBridge mixinWorld, final IBlockState notifyState, final BlockPos notifyPos,
        final Block sourceBlock, final BlockPos sourcePos) {
        if (!SpongeImplHooks.isMainThread()) {
            // lol no, report the block change properly
            new PrettyPrinter(60).add("Illegal Async PhaseTracker Access").centre().hr()
                .addWrapped(ASYNC_TRACKER_ACCESS)
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
            if (!((BlockBridge) notifyState.func_177230_c()).bridge$hasNeighborChangedLogic()) {
                // A little short-circuit so we do not waste expense to call neighbor notifications on blocks that do
                // not override the method neighborChanged
                return;
            }
            // If the phase state does not want to allow neighbor notifications to leak while processing,
            // it needs to be able to do so. It will replay the notifications in the order in which they were received,
            // such that the notification will be sent out in the same order as the block changes that may have taken place.
            if ((ShouldFire.CHANGE_BLOCK_EVENT || ShouldFire.NOTIFY_NEIGHBOR_BLOCK_EVENT) && state.doesCaptureNeighborNotifications(peek)) {
                peek.getCapturedBlockSupplier().captureNeighborNotification(mixinWorld, notifyState, notifyPos, sourceBlock, sourcePos);
                return;
            }
            state.associateNeighborStateNotifier(peek, sourcePos, notifyState.func_177230_c(), notifyPos, ((WorldServer) mixinWorld), PlayerTracker.Type.NOTIFIER);
            final LocatableBlock block = new SpongeLocatableBlockBuilder()
                .world(((World) mixinWorld))
                .position(sourcePos.func_177958_n(), sourcePos.func_177956_o(), sourcePos.func_177952_p())
                .state((BlockState) sourceBlock.func_176223_P()).build();
            try (final NeighborNotificationContext context = TickPhase.Tick.NEIGHBOR_NOTIFY.createPhaseContext()
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
                if (PhaseTracker.checkMaxBlockProcessingDepth(state, peek, context.getDepth())) {
                    return;
                }
                // Sponge End

                notifyState.func_189546_a(((WorldServer) mixinWorld), notifyPos, sourceBlock, sourcePos);
            }
        } catch (final Throwable throwable) {
            final CrashReport crashreport = CrashReport.func_85055_a(throwable, "Exception while updating neighbours");
            final CrashReportCategory crashreportcategory = crashreport.func_85058_a("Block being updated");
            crashreportcategory.func_189529_a("Source block type", () -> {
                try {
                    return String.format("ID #%d (%s // %s)", Block.func_149682_b(sourceBlock),
                        sourceBlock.func_149739_a(), sourceBlock.getClass().getCanonicalName());
                } catch (final Throwable var2) {
                    return "ID #" + Block.func_149682_b(sourceBlock);
                }
            });
            CrashReportCategory.func_175750_a(crashreportcategory, notifyPos, notifyState);
            throw new ReportedException(crashreport);
        }
    }

    /**
     * Replacement of {@link WorldServer#setBlockState(BlockPos, IBlockState, int)}
     * that adds cause tracking.
     *
     * @param pos The position of the block state to set
     * @param newState The new state
     * @param flag The notification flags
     * @return True if the block was successfully set (or captured)
     */
    @SuppressWarnings("rawtypes")
    public boolean setBlockState(final WorldServerBridge mixinWorld, final BlockPos pos, final IBlockState newState, final BlockChangeFlag flag) {
        if (!SpongeImplHooks.isMainThread()) {
            // lol no, report the block change properly
            new PrettyPrinter(60).add("Illegal Async Block Change").centre().hr()
                .addWrapped(ASYNC_BLOCK_CHANGE_MESSAGE)
                .add()
                .add(" %s : %s", "World", mixinWorld)
                .add(" %s : %d, %d, %d", "Block Pos", pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p())
                .add(" %s : %s", "BlockState", newState)
                .add()
                .addWrapped("Sponge is not going to allow this block change to take place as doing so can "
                            + "lead to further issues, not just with sponge or plugins, but other mods as well.")
                .add()
                .add(new Exception("Async Block Change Detected"))
                .log(SpongeImpl.getLogger(), Level.ERROR);
            return false;
        }
        final SpongeBlockChangeFlag spongeFlag = (SpongeBlockChangeFlag) flag;
        final net.minecraft.world.World minecraftWorld = (WorldServer) mixinWorld;

        // Vanilla start - get the chunk
        final Chunk chunk = minecraftWorld.func_175726_f(pos);
        // Sponge - double check the chunk is not empty.
        // It is now possible for setBlockState to be called on an empty chunk due to our optimization
        // for returning empty chunks when we don't want a chunk to load.
        // If chunk is empty, we simply return to avoid any further logic.
        if (chunk.func_76621_g()) {
            return false;
        }
        // Sponge End
        final IBlockState currentState = chunk.func_177435_g(pos);
        // Forge patches - allows getting the light changes to check for relighting.
        final int oldLight = SpongeImplHooks.getChunkPosLight(currentState, minecraftWorld, pos);
        final int oldOpacity = SpongeImplHooks.getBlockLightOpacity(currentState, minecraftWorld, pos);

        // Sponge Start - micro optimization to avoid calling extra stuff on the same block state instance
        if (currentState == newState) {
            // Some micro optimization in case someone is trying to set the new state to the same as current
            final SpongeProxyBlockAccess proxyAccess = mixinWorld.bridge$getProxyAccess();
            if (proxyAccess.hasProxy() && proxyAccess.func_180495_p(pos) != currentState) {
                proxyAccess.onChunkChanged(pos, newState);
            }
            return false;
        }

        final PhaseContext<?> context = this.stack.peek();
        final IPhaseState<?> phaseState = context.state;
        final boolean isComplete = phaseState == GeneralPhase.State.COMPLETE;
        // Do a sanity check, if we're not in any phase state that accepts block changes, well, why the hell are
        // we doing any changes?? The changes themselves will still go through, but we want to be as verbose
        // about those changes as possible, if we're configured to do so.
        if (isComplete && SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().isVerbose()) { // Fail fast.
            // The random occurrence that we're told to complete a phase
            // while a world is being changed unknowingly.
//            this.printUnexpectedBlockChange(mixinWorld, pos, currentState, newState);
        }
        // We can allow the block to get changed, regardless how it's captured, not captured, etc.
        // because ChunkMixin will perform the necessary changes, and appropriately prevent any specific
        // physics handling.

        final ChunkBridge mixinChunk = (ChunkBridge) chunk;
        // Sponge - Use our mixin method that allows using the BlockChangeFlag.

        final IBlockState originalBlockState = mixinChunk.bridge$setBlockState(pos, newState, currentState, spongeFlag);
        // Sponge End
        if (originalBlockState == null) {
            return false;
        }

        // else { // Sponge - unnecessary formatting
        // Forge changes the BlockState.getLightOpacity to use Forge's hook.
        if (SpongeImplHooks.getBlockLightOpacity(newState, minecraftWorld, pos) != oldOpacity || SpongeImplHooks.getChunkPosLight(newState, minecraftWorld, pos) != oldLight) {
            // Sponge - End
            minecraftWorld.field_72984_F.func_76320_a("checkLight");
            minecraftWorld.func_175664_x(pos);
            minecraftWorld.field_72984_F.func_76319_b();
        }

        // Sponge Start - At this point, we can stop and check for captures.
        //  by short circuiting here, we avoid additional block processing that would otherwise
        //  have potential side effects (and ChunkMixin#bridge$setBlockState does a wonderful job at avoiding
        //  unnecessary logic in those cases).
        if (((IPhaseState) phaseState).doesBulkBlockCapture(context) && ShouldFire.CHANGE_BLOCK_EVENT) {
            // Basically at this point, there's nothing left for us to do since
            // ChunkMixin will capture the block change, and submit it to be
            // "captured". It's only when there's immediate block event
            // processing that we need to actually create the event and process
            // that transaction.
            return true;
        }
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
                this.printBlockTrackingException(context, phaseState, e);
                return false;
            }
        }
        // Sponge End - continue with vanilla mechanics

        // Sponge - Use SpongeFlag. Inline world.isRemote since it's checked, and use the BlockChangeFlag#isNotifyClients()) And chunks are never null
        // flags & 2 is replaced with BlockChangeFlag#isNotifyClients
        // !this.isRemote is guaranteed since we are on the server
        // chunks are never null
        // if ((flags & 2) != 0 && (!this.isRemote || (flags & 4) == 0) && (chunk == null || chunk.isPopulated()))
        if (spongeFlag.isNotifyClients() && chunk.func_150802_k()) {
            // Sponge End
            minecraftWorld.func_184138_a(pos, originalBlockState, newState, spongeFlag.getRawFlag());
        }

        final Block block = newState.func_177230_c();
        if (spongeFlag.updateNeighbors()) { // Sponge - Replace flags & 1 != 0 with BlockChangeFlag#updateNeighbors
            minecraftWorld.func_175722_b(pos, originalBlockState.func_177230_c(), true);

            if (newState.func_185912_n()) {
                minecraftWorld.func_175666_e(pos, block);
            }
        } else if ( spongeFlag.notifyObservers()) { // Sponge - Replace flags & 16 == 0 with BlockChangeFlag#notifyObservers.
            minecraftWorld.func_190522_c(pos, block);
        }

        return true;
        // } // Sponge - unnecessary formatting

    }

    /**
     * This is the replacement of {@link WorldServer#spawnEntity(net.minecraft.entity.Entity)}
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
    public boolean spawnEntity(final WorldServer world, final net.minecraft.entity.Entity entity) {
        checkNotNull(entity, "Entity cannot be null!");
        // Forge requires checking if we're restoring in a world to avoid spawning item drops.
        if (entity instanceof EntityItem && SpongeImplHooks.isRestoringBlocks(world)) {
            return false;
        }

        // Sponge Start - handle construction phases
        if (((EntityBridge) entity).bridge$isConstructing()) {
            ((EntityBridge) entity).bridge$fireConstructors();
        }

        final WorldServerBridge mixinWorldServer = (WorldServerBridge) world;
        final PhaseContext<?> context = this.stack.peek();
        final IPhaseState<?> phaseState = context.state;
        final boolean isForced = entity.field_98038_p || entity instanceof EntityPlayer;

        // Certain phases disallow entity spawns (such as block restoration)
        if (!isForced && !phaseState.doesAllowEntitySpawns()) {
            return false;
        }

        // Sponge End - continue with vanilla mechanics
        final int chunkX = MathHelper.func_76128_c(entity.field_70165_t / 16.0D);
        final int chunkZ = MathHelper.func_76128_c(entity.field_70161_v / 16.0D);

        if (!isForced && !((WorldServerAccessor) world).accessor$isChunkLoaded(chunkX, chunkZ, true)) {
            return false;
        }
        if (entity instanceof EntityPlayer) {
            final EntityPlayer entityplayer = (EntityPlayer) entity;
            world.field_73010_i.add(entityplayer);
            world.func_72854_c();
            SpongeImplHooks.firePlayerJoinSpawnEvent((EntityPlayerMP) entityplayer);
        } else {
            // Sponge start - check for vanilla owner
            if (entity instanceof IEntityOwnable) {
                final IEntityOwnable ownable = (IEntityOwnable) entity;
                final net.minecraft.entity.Entity owner = ownable.func_70902_q();
                if (owner instanceof EntityPlayer) {
                    context.owner = (User) owner;
                    if (entity instanceof OwnershipTrackedBridge) {
                        ((OwnershipTrackedBridge) entity).tracked$setOwnerReference((User) owner);
                    } else {
                        ((Entity) entity).setCreator(ownable.func_184753_b());
                    }
                }
            } else if (entity instanceof EntityThrowable) {
                final EntityThrowable throwable = (EntityThrowable) entity;
                final EntityLivingBase thrower = throwable.func_85052_h();
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
                            ((Entity) entity).setCreator(user.getUniqueId());
                        }
                    }
                }
            }
            // Sponge end
        }
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
                    return ((IPhaseState) phaseState).spawnEntityOrCapture(context, (Entity) entity, chunkX, chunkZ);
                } catch (final Exception | NoClassDefFoundError e) {
                    // Just in case something really happened, we should print a nice exception for people to
                    // paste us
                    this.printExceptionSpawningEntity(context, e);
                    return false;
                }
            }
        }
        final net.minecraft.entity.Entity customEntity = SpongeImplHooks.getCustomEntityIfItem(entity);
        final net.minecraft.entity.Entity finalEntityToSpawn = customEntity == null ? entity : customEntity;
        // Sponge end - continue on with the checks.
        world.func_72964_e(chunkX, chunkZ).func_76612_a(finalEntityToSpawn);
        world.field_72996_f.add(finalEntityToSpawn);
        // Sponge - Cannot add onEntityAdded to the access transformer because forge makes it public
        ((WorldServerAccessor) world).accessor$onEntityAdded(finalEntityToSpawn);
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
    public boolean spawnEntityWithCause(final World world, final Entity entity) {
        checkNotNull(entity, "Entity cannot be null!");

        // Sponge Start - handle construction phases
        if (((EntityBridge) entity).bridge$isConstructing()) {
            ((EntityBridge) entity).bridge$fireConstructors();
        }

        final net.minecraft.entity.Entity minecraftEntity = (net.minecraft.entity.Entity) entity;
        final WorldServer worldServer = (WorldServer) world;
        final WorldServerBridge mixinWorldServer = (WorldServerBridge) worldServer;
        // Sponge End - continue with vanilla mechanics

        final int chunkX = MathHelper.func_76128_c(minecraftEntity.field_70165_t / 16.0D);
        final int chunkZ = MathHelper.func_76128_c(minecraftEntity.field_70161_v / 16.0D);
        final boolean isForced = minecraftEntity.field_98038_p || minecraftEntity instanceof EntityPlayer;

        if (!isForced && !((WorldServerAccessor) world).accessor$isChunkLoaded(chunkX, chunkZ, true)) {
            return false;
        }
        // Sponge Start - throw an event
        final List<Entity> entities = new ArrayList<>(1); // We need to use an arraylist so that filtering will work.
        entities.add(entity);

        final SpawnEntityEvent.Custom
            event =
            SpongeEventFactory.createSpawnEntityEventCustom(Sponge.getCauseStackManager().getCurrentCause(), entities);
        SpongeImpl.postEvent(event);
        if (entity instanceof EntityPlayer || !event.isCancelled()) {
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
        if (Sponge.isServerAvailable() && (Sponge.getServer().isMainThread() || SpongeImpl.getServer().func_71241_aa())) {
            return false;
        }
        // We aren't in the server thread at this point, and an entity is spawning on the server....
        // We will DEFINITELY be doing bad things otherwise. We need to artificially capture here.
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().captureEntitiesAsync()) {
            // Print a pretty warning about not capturing an async spawned entity, but don't care about spawning.
            if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().isVerbose()) {
                return true;
            }
            // Just checking if we've already printed once about it.
            // If we have, we don't want to print any more times.
            if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().verboseErrors() && PhaseTracker.getInstance().hasPrintedAsyncEntities) {
                return true;
            }
            // Otherwise, let's print out either the first time, or several more times.
            new PrettyPrinter(60)
                .add("Async Entity Spawn Warning").centre().hr()
                .add("An entity was attempting to spawn off the \"main\" server thread")
                .add()
                .add("Details of the spawning are disabled according to the Sponge")
                .add("configuration file. A stack trace of the attempted spawn should")
                .add("provide information about how it was being spawned. Sponge is")
                .add("currently configured to NOT attempt to capture this spawn and")
                .add("spawn the entity at an appropriate time, while on the main server")
                .add("thread.")
                .add()
                .add("Details of the spawn:")
                .add("%s : %s", "Entity", entity)
                .add("Stacktrace")
                .add(new Exception("Async entity spawn attempt"))
                .trace(SpongeImpl.getLogger(), Level.WARN);
            PhaseTracker.getInstance().hasPrintedAsyncEntities = true;
            return true;
        }
        ASYNC_CAPTURED_ENTITIES.add((net.minecraft.entity.Entity) entity);
        // At this point we can print an exception about it, if we are told to.
        // Print a pretty warning about not capturing an async spawned entity, but don't care about spawning.
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().isVerbose()) {
            return true;
        }
        // Just checking if we've already printed once about it.
        // If we have, we don't want to print any more times.
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().verboseErrors() && PhaseTracker.getInstance().hasPrintedAsyncEntities) {
            return true;
        }
        // Otherwise, let's print out either the first time, or several more times.
        new PrettyPrinter(60)
            .add("Async Entity Spawn Warning").centre().hr()
            .add("An entity was attempting to spawn off the \"main\" server thread")
            .add()
            .add("Delayed spawning is ENABLED for Sponge.")
            .add("The entity is safely captured by Sponge while off the main")
            .add("server thread, and therefore will be spawned the next tick.")
            .add("Some cases where a mod is expecting the entity back while")
            .add("async can cause issues with said mod.")
            .add()
            .add("Details of the spawn:")
            .add("%s : %s", "Entity", entity)
            .add("Stacktrace")
            .add(new Exception("Async entity spawn attempt"))
            .trace(SpongeImpl.getLogger(), Level.WARN);
        PhaseTracker.getInstance().hasPrintedAsyncEntities = true;


        return true;
    }

    public static boolean checkMaxBlockProcessingDepth(final IPhaseState<?> state, final PhaseContext<?> context, final int currentDepth) {
        final SpongeConfig<GlobalConfig> globalConfigAdapter = SpongeImpl.getGlobalConfigAdapter();
        final PhaseTrackerCategory trackerConfig = globalConfigAdapter.getConfig().getPhaseTracker();
        int maxDepth = trackerConfig.getMaxBlockProcessingDepth();
        if (maxDepth == 100 && state == TickPhase.Tick.NEIGHBOR_NOTIFY) {
            maxDepth = 1000;
            trackerConfig.resetMaxDepthTo1000();
            globalConfigAdapter.save();
        }
        if (currentDepth < maxDepth) {
            return false;
        }

        final PhaseTracker tracker = PhaseTracker.getInstance();
        if (!trackerConfig.isVerbose() && tracker.printedExceptionForMaximumProcessDepth.contains(state)) {
            // We still want to abort processing even if we're not logigng an error
            return true;
        }

        tracker.printedExceptionForMaximumProcessDepth.add(state);
        final String message = String.format("Sponge is still trying to process captured blocks after %s iterations of depth-first processing."
                                            + " This is likely due to a mod doing something unusual.", currentDepth);
        tracker.printMessageWithCaughtException("Maximum block processing depth exceeded!", message, state, context, null);

        return true;
    }

    public void ensureEmpty() {
        if (!this.stack.isEmpty()) {
            final PrettyPrinter printer = new PrettyPrinter(60);
            printer.add("Phases Not Completed").centre().hr();
            printer.add("One or more phases were started but were not properly completed by the end of the server tick. They will be automatically "
                    + "closed, but this is an issue that should be reported to Sponge.");
            this.stack.forEach(data -> PHASE_PRINTER.accept(printer, data));
            printer.add();
            this.generateVersionInfo(printer);
            printer.trace(System.err, SpongeImpl.getLogger(), Level.ERROR);

            while (!this.stack.isEmpty()) {
                this.getCurrentContext().close();
            }
        }
    }
}
