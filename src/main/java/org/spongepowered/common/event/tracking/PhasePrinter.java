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

import org.apache.logging.log4j.Level;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.applaunch.config.common.CommonConfig;
import org.spongepowered.common.applaunch.config.common.PhaseTrackerCategory;
import org.spongepowered.common.applaunch.config.core.ConfigHandle;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;
import org.spongepowered.common.bridge.server.level.ServerLevelBridge;
import org.spongepowered.common.bridge.world.TrackedWorldBridge;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.util.PrettyPrinter;
import org.spongepowered.plugin.PluginContainer;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

public final class PhasePrinter {

    private static final boolean IN_DEVELOPMENT = Launch.getInstance().isDeveloperEnvironment();
    static final String ASYNC_BLOCK_CHANGE_MESSAGE = "Sponge adapts the vanilla handling of block changes to power events and plugins "
                                                + "such that it follows the known fact that block changes MUST occur on the server "
                                                + "thread (even on clients, this exists as the InternalServer thread). It is NOT "
                                                + "possible to change this fact and must be reported to the offending mod for async "
                                                + "issues.";
    public static final String ASYNC_TRACKER_ACCESS = "Sponge adapts the vanilla handling of various processes, such as setting a block "
                                                      + "or spawning an entity. Sponge is designed around the concept that Minecraft is "
                                                      + "primarily performing these operations on the \"server thread\". Because of this "
                                                      + "Sponge is safeguarding common access to the PhaseTracker as the entrypoint for "
                                                      + "performing these sort of changes.";
    private static boolean hasPrintedEmptyOnce = false;
    private static boolean hasPrintedAboutRunnawayPhases = false;
    private static boolean hasPrintedAsyncEntities = false;
    private static int printRunawayCount = 0;
    private static final List<IPhaseState<@NonNull ?>> printedExceptionsForBlocks = new ArrayList<>();
    private static final List<IPhaseState<@NonNull ?>> printedExceptionsForEntities = new ArrayList<>();
    private static final List<Tuple<IPhaseState<@NonNull ?>, IPhaseState<@NonNull ?>>> completedIncorrectStates = new ArrayList<>();
    private static final List<IPhaseState<@NonNull ?>> printedExceptionsForState = new ArrayList<>();
    static final Set<IPhaseState<@NonNull ?>> printedExceptionsForUnprocessedState = new HashSet<>();
    static final Set<IPhaseState<@NonNull ?>> printedExceptionForMaximumProcessDepth = new HashSet<>();
    static final PhaseStack EMPTY = new PhaseStack();


    public static final BiConsumer<PrettyPrinter, PhaseContext<@NonNull ?>> CONTEXT_PRINTER = (printer, context) ->
        context.printCustom(printer, 4);

    static final BiConsumer<PrettyPrinter, PhaseContext<@NonNull ?>> PHASE_PRINTER = (printer, context) -> {
            printer.add("  - Phase: %s", context.state);
            printer.add("    Context:");
            context.printCustom(printer, 4);
            context.printTrace(printer);
        };

    static void printNullSourceForBlock(final ServerLevel worldServer, final BlockPos pos, final Block blockIn, final BlockPos otherPos,
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
            .add(" %s : %s", "World", ((org.spongepowered.api.world.server.ServerWorld) worldServer).key())
            .add(" %s : %s", "Source Block Recovered", blockIn)
            .add(" %s : %s", "Notified Position", otherPos).add();

        PhasePrinter.printPhaseStackWithException(instance.stack, printer, e);
        printer
            .log(SpongeCommon.getLogger(), Level.WARN);
    }


    static void printUnexpectedBlockChange(final ServerLevelBridge mixinWorld, final BlockPos pos, final net.minecraft.world.level.block.state.BlockState currentState,
                                            final net.minecraft.world.level.block.state.BlockState newState) {
        if (!SpongeConfigs.getCommon().get().phaseTracker.verbose) {
            return;
        }
        final PrettyPrinter printer = new PrettyPrinter(60).add("Unexpected World Change Detected!").centre().hr()
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
            .add(new Exception());
        if (PhasePrinter.IN_DEVELOPMENT) {
            printer.print(System.err);
        } else {
            printer.log(SpongeCommon.getLogger(), Level.ERROR);
        }
    }


    static void printExceptionSpawningEntity(final PhaseTracker tracker, final PhaseContext<@NonNull ?> context, final Throwable e) {
        if (!SpongeConfigs.getCommon().get().phaseTracker.verbose && !PhasePrinter.printedExceptionsForEntities.isEmpty()) {
            if (PhasePrinter.printedExceptionsForEntities.contains(context.state)) {
                return;
            }
        }
        final PrettyPrinter printer = new PrettyPrinter(60).add("Exception attempting to capture or spawn an Entity!").centre().hr();
        PhasePrinter.printPhasestack(tracker, context, e, printer);
        printer.log(SpongeCommon.getLogger(), Level.ERROR);
        if (!SpongeConfigs.getCommon().get().phaseTracker.verbose) {
            PhasePrinter.printedExceptionsForEntities.add(context.state);
        }
    }

    static void printNullSourceBlockWithTile(
            final BlockPos pos, final Block blockIn, final BlockPos otherPos, final ResourceLocation type, final boolean useTile,
            final NullPointerException e) {
        final PhaseTracker instance = PhaseTracker.getInstance();
        final PrettyPrinter printer = new PrettyPrinter(60).add("Null Source Block on TileEntity!").centre().hr()
            .addWrapped("Hey, Sponge is saving the game from crashing because a TileEntity "
                        + "is sending out a 'null' Block as it's source (more likely) and "
                        + "attempting to perform a neighbor notification with it. Because "
                        + "this is guaranteed to lead to a crash or a spam of reports, "
                        + "Sponge is going ahead and fixing the issue. The offending Tile "
                        + "is " + type.toString())
            .add()
            .add("%s : %s", "Source position", pos)
            .add("%s : %s", "Source TileEntity", type)
            .add("%s : %s", "Recovered using TileEntity as Source", useTile)
            .add("%s : %s", "Source Block Recovered", blockIn)
            .add("%s : %s", "Notified Position", otherPos);
        PhasePrinter.printPhaseStackWithException(instance.stack, printer, e);
        printer
            .log(SpongeCommon.getLogger(), Level.WARN);
    }

    static void printNullSourceBlockNeighborNotificationWithNoTileSource(final BlockPos pos, final Block blockIn, final BlockPos otherPos,
                                                                                final NullPointerException e) {
        final PhaseTracker instance = PhaseTracker.getInstance();
        final PrettyPrinter printer = new PrettyPrinter(60).add("Null Source Block on TileEntity!").centre().hr()
            .addWrapped("Hey, Sponge is saving the game from crashing because a TileEntity "
                        + "is sending out a 'null' Block as it's source (more likely) and "
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
        PhasePrinter.printPhaseStackWithException(instance.stack, printer, e);
        printer
            .log(SpongeCommon.getLogger(), Level.WARN);
    }

    static void printPhaseStackWithException(final PhaseStack stack, final PrettyPrinter printer, final Throwable e) {
        stack.forEach(data -> PhasePrinter.PHASE_PRINTER.accept(printer, data));
        printer.add()
            .add(" %s :", "StackTrace")
            .add(e)
            .add();
        PhasePrinter.generateVersionInfo(printer);
    }

    static void printBlockTrackingException(final PhaseTracker tracker, final PhaseContext<@NonNull ?> phaseData, final IPhaseState<@NonNull ?> phaseState, final Throwable e) {
        if (!SpongeConfigs.getCommon().get().phaseTracker.verbose && !PhasePrinter.printedExceptionsForBlocks.isEmpty()) {
            if (PhasePrinter.printedExceptionsForBlocks.contains(phaseState)) {
                return;
            }
        }
        final PrettyPrinter printer = new PrettyPrinter(60).add("Exception attempting to capture a block change!").centre().hr();
        PhasePrinter.printPhasestack(tracker, phaseData, e, printer);
        if (PhasePrinter.IN_DEVELOPMENT) {
            printer.print(System.err);
        } else {
            printer.log(SpongeCommon.getLogger(), Level.ERROR);
        }
        if (!SpongeConfigs.getCommon().get().phaseTracker.verbose) {
            PhasePrinter.printedExceptionsForBlocks.add(phaseState);
        }
    }

    static void printPhasestack(final PhaseTracker tracker, final PhaseContext<@NonNull ?> phaseData, final Throwable e, final PrettyPrinter printer) {
        printer.addWrapped(60, "%s :", "PhaseContext");
        PhasePrinter.CONTEXT_PRINTER.accept(printer, phaseData);
        printer.addWrapped(60, "%s :", "Phases remaining");
        tracker.stack.forEach(data -> PhasePrinter.PHASE_PRINTER.accept(printer, data));
        printer.add("Stacktrace:");
        printer.add(e);
    }

    public static void printMessageWithCaughtException(final PhaseTracker tracker, final String header, final String subheader, final Throwable e) {
        PhasePrinter.printMessageWithCaughtException(tracker.stack, header, subheader, tracker.getPhaseContext().state, tracker.getPhaseContext(), e);
    }

    public static void printMessageWithCaughtException(final PhaseTracker tracker, final String header, final String subHeader, final IPhaseState<@NonNull ?> state, final PhaseContext<@NonNull ?> context, final @Nullable Throwable t) {
        PhasePrinter.printMessageWithCaughtException(tracker.stack, header, subHeader, state, context, t);
    }

    public static void printMessageWithCaughtException(final PhaseStack stack, final String header, final String subHeader, final IPhaseState<@NonNull ?> state, final PhaseContext<@NonNull ?> context, final @Nullable Throwable t) {
        final PrettyPrinter printer = new PrettyPrinter(60);
        printer.add(header).centre().hr()
                .add("%s %s", subHeader, state)
                .addWrapped(60, "%s :", "PhaseContext");
        PhasePrinter.CONTEXT_PRINTER.accept(printer, context);
        printer.addWrapped(60, "%s :", "Phases remaining");
        stack.forEach(data -> PhasePrinter.PHASE_PRINTER.accept(printer, data));
        if (t != null) {
            printer.add("Stacktrace:")
                    .add(t);
            if (t.getCause() != null) {
                printer.add(t.getCause());
            }
        }
        printer.add();
        PhasePrinter.generateVersionInfo(printer);
        if (PhasePrinter.IN_DEVELOPMENT) {
            printer.print(System.err);
        } else {
            printer.log(SpongeCommon.getLogger(), Level.ERROR);
        }
    }

    static void printExceptionFromPhase(final PhaseStack stack, final Throwable e, final PhaseContext<@NonNull ?> context) {
        if (!SpongeConfigs.getCommon().get().phaseTracker.verbose && !PhasePrinter.printedExceptionsForState.isEmpty()) {
            for (final IPhaseState<@NonNull ?> iPhaseState : PhasePrinter.printedExceptionsForState) {
                if (context.state == iPhaseState) {
                    return;
                }
            }
        }
        final PrettyPrinter printer = new PrettyPrinter(60).add("Exception occurred during a PhaseState").centre().hr()
            .add("Sponge's tracking system makes a best effort to not throw exceptions randomly but sometimes it is inevitable. In most "
                    + "cases, something else triggered this exception and Sponge prevented a crash by catching it. The following stacktrace can be "
                    + "used to help pinpoint the cause.").hr()
            .add("The PhaseState having an exception: %s", context.state)
            .add("The PhaseContext:")
            ;
        context.printCustom(printer, 4);
        PhasePrinter.printPhaseStackWithException(stack, printer, e);

        if (PhasePrinter.IN_DEVELOPMENT) {
            printer.print(System.err);
        } else {
            printer.log(SpongeCommon.getLogger(), Level.ERROR);
        }
        if (!SpongeConfigs.getCommon().get().phaseTracker.verbose) {
            PhasePrinter.printedExceptionsForState.add(context.state);
        }
    }

    static void printUnprocessedPhaseContextObjects(final PhaseStack stack, final IPhaseState<@NonNull ?> state, final PhaseContext<@NonNull ?> context) {
        PhasePrinter.printMessageWithCaughtException(stack, "Failed to process all PhaseContext captured!",
                "During the processing of a phase, certain objects were captured in a PhaseContext. All of them should have been removed from the PhaseContext by this point",
                state, context, null);
    }

    static void printRunawayPhase(final PhaseStack stack, final IPhaseState<@NonNull ?> state, final PhaseContext<@NonNull ?> context) {
        if (!SpongeConfigs.getCommon().get().phaseTracker.verbose && !PhasePrinter.hasPrintedAboutRunnawayPhases) {
            // Avoiding spam logs.
            return;
        }
        final PrettyPrinter printer = new PrettyPrinter(60);
        printer.add("Switching Phase").centre().hr();
        printer.addWrapped(60, "Detecting a runaway phase! Potentially a problem where something isn't completing a phase!!!");
        printer.add("  %s : %s", "Entering State", state);
        PhasePrinter.CONTEXT_PRINTER.accept(printer, context);
        printer.addWrapped(60, "%s :", "Phases remaining");
        PhasePrinter.printPhaseStackWithException(stack, printer, new Exception("RunawayPhase"));
        if (PhasePrinter.IN_DEVELOPMENT) {
            printer.print(System.err);
        } else {
            printer.log(SpongeCommon.getLogger(), Level.ERROR);
        }
        if (!SpongeConfigs.getCommon().get().phaseTracker.verbose && PhasePrinter.printRunawayCount++ > SpongeConfigs.getCommon().get().phaseTracker.maximumPrintedRunawayCounts) {
            PhasePrinter.hasPrintedAboutRunnawayPhases = true;
        }
    }

    static void printRunAwayPhaseCompletion(final PhaseStack stack, final IPhaseState<@NonNull ?> state) {
        if (!SpongeConfigs.getCommon().get().phaseTracker.verbose && !PhasePrinter.hasPrintedAboutRunnawayPhases) {
            // Avoiding spam logs.
            return;
        }
        final PrettyPrinter printer = new PrettyPrinter(60);
        printer.add("Completing Phase").centre().hr();
        printer.add("Detecting a runaway phase! Potentially a problem "
                               + "where something isn't completing a phase!!! Sponge will stop printing"
                               + "after three more times to avoid generating extra logs");
        printer.add();
        printer.add("%s : %s", "Completing phase", state);
        printer.add(" Phases Remaining:");
        PhasePrinter.printPhaseStackWithException(stack, printer, new Exception("RunawayPhase"));
        if (PhasePrinter.IN_DEVELOPMENT) {
            printer.print(System.err);
        } else {
            printer.log(SpongeCommon.getLogger(), Level.ERROR);
        }
        if (!SpongeConfigs.getCommon().get().phaseTracker.verbose && PhasePrinter.printRunawayCount++ > 3) {
            PhasePrinter.hasPrintedAboutRunnawayPhases = true;
        }
    }

    static void generateVersionInfo(final PrettyPrinter printer) {
        for (final PluginContainer pluginContainer : Launch.getInstance().getLauncherPlugins()) {
            printer.add("%s : %s", pluginContainer.metadata().name().get(), pluginContainer.metadata().version());
        }
    }

    static void printIncorrectPhaseCompletion(final PhaseStack stack, final IPhaseState<@NonNull ?> prevState, final IPhaseState<@NonNull ?> state) {
        if (!SpongeConfigs.getCommon().get().phaseTracker.verbose && !PhasePrinter.completedIncorrectStates.isEmpty()) {
            for (final Tuple<IPhaseState<@NonNull ?>, IPhaseState<@NonNull ?>> tuple : PhasePrinter.completedIncorrectStates) {
                if ((tuple.first().equals(prevState)
                        && tuple.second().equals(state))) {
                    // we've already printed once about the previous state and the current state
                    // being completed incorrectly. only print it once.
                    return;
                }
            }
        }

        final PrettyPrinter printer = new PrettyPrinter(60).add("Completing incorrect phase").centre().hr()
                .add("Sponge's tracking system is very dependent on knowing when"
                        + " a change to any world takes place, however, we are attempting"
                        + " to complete a \"phase\" other than the one we most recently entered."
                        + " This is an error usually on Sponge's part, so a report"
                        + " is required on the issue tracker on GitHub.").hr()
                .add("Expected to exit phase: %s", prevState)
                .add("But instead found phase: %s", state)
                .add("StackTrace:")
                .add(new Exception());
        printer.add(" Phases Remaining:");
        PhasePrinter.printPhaseStackWithException(stack, printer, new Exception("Incorrect Phase Completion"));
        if (PhasePrinter.IN_DEVELOPMENT) {
            printer.print(System.err);
        } else {
            printer.log(SpongeCommon.getLogger(), Level.ERROR);
        }
        if (!SpongeConfigs.getCommon().get().phaseTracker.verbose) {
            PhasePrinter.completedIncorrectStates.add(new Tuple<>(prevState, state));
        }
    }

    static void printEmptyStackOnCompletion(final PhaseContext<@NonNull ?> context) {
        if (PhasePrinter.hasPrintedEmptyOnce) {
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
        PhasePrinter.PHASE_PRINTER.accept(printer, context);
        printer.add();
        PhasePrinter.generateVersionInfo(printer);
        if (PhasePrinter.IN_DEVELOPMENT) {
            printer.print(System.err);
        } else {
            printer.log(SpongeCommon.getLogger(), Level.ERROR);
        }
        if (!SpongeConfigs.getCommon().get().phaseTracker.verbose) {
            PhasePrinter.hasPrintedEmptyOnce = true;
        }
    }

    static void printNonEmptyStack(final PhaseStack stack) {
        final PrettyPrinter printer = new PrettyPrinter(60);
        printer.add("Phases Not Completed").centre().hr();
        printer.add("One or more phases were started but were not properly completed by the end of the server tick. They will be automatically "
                + "closed, but this is an issue that should be reported to Sponge.");
        stack.forEach(data -> PhasePrinter.PHASE_PRINTER.accept(printer, data));
        printer.add();
        PhasePrinter.generateVersionInfo(printer);
        if (PhasePrinter.IN_DEVELOPMENT) {
            printer.print(System.err);
        } else {
            printer.log(SpongeCommon.getLogger(), Level.ERROR);
        }
    }

    static void printAsyncBlockChange(final TrackedWorldBridge mixinWorld, final BlockPos pos, final net.minecraft.world.level.block.state.BlockState newState) {
        new PrettyPrinter(60).add("Illegal Async Block Change").centre().hr()
            .addWrapped(PhasePrinter.ASYNC_BLOCK_CHANGE_MESSAGE)
            .add()
            .add(" %s : %s", "World", mixinWorld)
            .add(" %s : %d, %d, %d", "Block Pos", pos.getX(), pos.getY(), pos.getZ())
            .add(" %s : %s", "BlockState", newState)
            .add()
            .addWrapped("Sponge is not going to allow this block change to take place as doing so can "
                        + "lead to further issues, not just with sponge or plugins, but other mods as well.")
            .add()
            .add(new Exception("Async Block Change Detected"))
            .log(SpongeCommon.getLogger(), Level.ERROR);
    }

    static void printAsyncEntitySpawn(final Entity entity) {
        // We aren't in the server thread at this point, and an entity is spawning on the server....
        // We will DEFINITELY be doing bad things otherwise. We need to artificially capture here.
        if (!SpongeConfigs.getCommon().get().phaseTracker.captureAsyncSpawningEntities) {
            // Print a pretty warning about not capturing an async spawned entity, but don't care about spawning.
            if (!SpongeConfigs.getCommon().get().phaseTracker.verbose) {
                return;
            }
            // Just checking if we've already printed once about it.
            // If we have, we don't want to print any more times.
            if (!SpongeConfigs.getCommon().get().phaseTracker.verboseErrors && PhasePrinter.hasPrintedAsyncEntities) {
                return;
            }
            // Otherwise, let's print out either the first time, or several more times.
            final PrettyPrinter printer = new PrettyPrinter(60)
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
                .add(new Exception("Async entity spawn attempt"));
            if (PhasePrinter.IN_DEVELOPMENT) {
                printer.print(System.err);
            } else {
                printer.log(SpongeCommon.getLogger(), Level.ERROR);
            }
            PhasePrinter.hasPrintedAsyncEntities = true;
            return;
        }
        PhaseTracker.ASYNC_CAPTURED_ENTITIES.add((net.minecraft.world.entity.Entity) entity);
        // At this point we can print an exception about it, if we are told to.
        // Print a pretty warning about not capturing an async spawned entity, but don't care about spawning.
        if (!SpongeConfigs.getCommon().get().phaseTracker.verbose) {
            return;
        }
        // Just checking if we've already printed once about it.
        // If we have, we don't want to print any more times.
        if (!SpongeConfigs.getCommon().get().phaseTracker.verboseErrors && PhasePrinter.hasPrintedAsyncEntities) {
            return;
        }
        // Otherwise, let's print out either the first time, or several more times.
        final PrettyPrinter printer = new PrettyPrinter(60)
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
            .add(new Exception("Async entity spawn attempt"));
        if (PhasePrinter.IN_DEVELOPMENT) {
            printer.print(System.err);
        } else {
            printer.log(SpongeCommon.getLogger(), Level.ERROR);
        }
        PhasePrinter.hasPrintedAsyncEntities = true;
    }

    static boolean checkMaxBlockProcessingDepth(final IPhaseState<@NonNull ?> state, final PhaseContext<@NonNull ?> context, final int currentDepth) {
        final ConfigHandle<CommonConfig> globalConfigAdapter = SpongeConfigs.getCommon();
        final PhaseTrackerCategory trackerConfig = globalConfigAdapter.get().phaseTracker;
        int maxDepth = trackerConfig.maxBlockProcessingDepth;
        if (currentDepth < maxDepth) {
            return false;
        }

        if (!trackerConfig.verbose && PhasePrinter.printedExceptionForMaximumProcessDepth.contains(state)) {
            // We still want to abort processing even if we're not logigng an error
            return true;
        }

        PhasePrinter.printedExceptionForMaximumProcessDepth.add(state);
        final String message = String.format("Sponge is still trying to process captured blocks after %s iterations of depth-first processing."
                                            + " This is likely due to a mod doing something unusual.", currentDepth);
        PhasePrinter.printMessageWithCaughtException(PhasePrinter.EMPTY, "Maximum block processing depth exceeded!", message, state, context, null);

        return true;
    }
}
