package org.spongepowered.common.event.tracking;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.block.entity.BlockEntityType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.category.PhaseTrackerCategory;
import org.spongepowered.common.config.type.GlobalConfig;
import org.spongepowered.common.event.tracking.phase.tick.TickPhase;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

final class PhasePrinter {

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
    private static boolean hasPrintedEmptyOnce = false;
    private static boolean hasPrintedAboutRunnawayPhases = false;
    private static boolean hasPrintedAsyncEntities = false;
    private static int printRunawayCount = 0;
    private static final List<IPhaseState<?>> printedExceptionsForBlocks = new ArrayList<>();
    private static final List<IPhaseState<?>> printedExceptionsForEntities = new ArrayList<>();
    private static final List<Tuple<IPhaseState<?>, IPhaseState<?>>> completedIncorrectStates = new ArrayList<>();
    private static final List<IPhaseState<?>> printedExceptionsForState = new ArrayList<>();
    static final Set<IPhaseState<?>> printedExceptionsForUnprocessedState = new HashSet<>();
    static final Set<IPhaseState<?>> printedExceptionForMaximumProcessDepth = new HashSet<>();
    static final PhaseStack EMPTY = new PhaseStack();


    public static final BiConsumer<PrettyPrinter, PhaseContext<?>> CONTEXT_PRINTER = (printer, context) ->
        context.printCustom(printer, 4);
    static final BiConsumer<PrettyPrinter, PhaseContext<?>> PHASE_PRINTER = (printer, context) -> {
            printer.add("  - Phase: %s", context.state);
            printer.add("    Context:");
            context.printCustom(printer, 4);
            context.printTrace(printer);
        };

    public static void printNullSourceForBlock(final ServerWorld worldServer, final BlockPos pos, final Block blockIn, final BlockPos otherPos,
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
            .add(" %s : %s", "World", ((World<?>) worldServer).getProperties().getName())
            .add(" %s : %s", "Source Block Recovered", blockIn)
            .add(" %s : %s", "Notified Position", otherPos).add();

        PhasePrinter.printPhaseStackWithException(instance.stack, printer, e);
        printer
            .log(SpongeImpl.getLogger(), Level.WARN);
    }


    static void printUnexpectedBlockChange(final ServerWorldBridge mixinWorld, final BlockPos pos, final net.minecraft.block.BlockState currentState,
                                            final net.minecraft.block.BlockState newState) {
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


    static void printExceptionSpawningEntity(final PhaseTracker tracker, final PhaseContext<?> context, final Throwable e) {
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().isVerbose() && !PhasePrinter.printedExceptionsForEntities.isEmpty()) {
            if (PhasePrinter.printedExceptionsForEntities.contains(context.state)) {
                return;
            }
        }
        final PrettyPrinter printer = new PrettyPrinter(60).add("Exception attempting to capture or spawn an Entity!").centre().hr();
        PhasePrinter.printPhasestack(tracker, context, e, printer);
        printer.log(SpongeImpl.getLogger(), Level.ERROR);
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().isVerbose()) {
            PhasePrinter.printedExceptionsForEntities.add(context.state);
        }
    }

    public static void printNullSourceBlockWithTile(
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
            .log(SpongeImpl.getLogger(), Level.WARN);
    }

    public static void printNullSourceBlockNeighborNotificationWithNoTileSource(final BlockPos pos, final Block blockIn, final BlockPos otherPos,
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
            .log(SpongeImpl.getLogger(), Level.WARN);
    }

    public static void printPhaseStackWithException(final PhaseStack stack, final PrettyPrinter printer, final Throwable e) {
        stack.forEach(data -> PhasePrinter.PHASE_PRINTER.accept(printer, data));
        printer.add()
            .add(" %s :", "StackTrace")
            .add(e)
            .add();
        PhasePrinter.generateVersionInfo(printer);
    }

    static void printBlockTrackingException(final PhaseTracker tracker, final PhaseContext<?> phaseData, final IPhaseState<?> phaseState, final Throwable e) {
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().isVerbose() && !PhasePrinter.printedExceptionsForBlocks.isEmpty()) {
            if (PhasePrinter.printedExceptionsForBlocks.contains(phaseState)) {
                return;
            }
        }
        final PrettyPrinter printer = new PrettyPrinter(60).add("Exception attempting to capture a block change!").centre().hr();
        PhasePrinter.printPhasestack(tracker, phaseData, e, printer);
        printer.trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().isVerbose()) {
            PhasePrinter.printedExceptionsForBlocks.add(phaseState);
        }
    }

    static void printPhasestack(final PhaseTracker tracker, final PhaseContext<?> phaseData, final Throwable e, final PrettyPrinter printer) {
        printer.addWrapped(60, "%s :", "PhaseContext");
        PhasePrinter.CONTEXT_PRINTER.accept(printer, phaseData);
        printer.addWrapped(60, "%s :", "Phases remaining");
        tracker.stack.forEach(data -> PhasePrinter.PHASE_PRINTER.accept(printer, data));
        printer.add("Stacktrace:");
        printer.add(e);
    }

    static void printMessageWithCaughtException(final PhaseStack stack, final String header, final String subHeader, final IPhaseState<?> state, final PhaseContext<?> context, @Nullable final Throwable t) {
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
        printer.trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
    }

    public static void printExceptionFromPhase(final PhaseStack stack, final Throwable e, final PhaseContext<?> context) {
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().isVerbose() && !PhasePrinter.printedExceptionsForState.isEmpty()) {
            for (final IPhaseState<?> iPhaseState : PhasePrinter.printedExceptionsForState) {
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
        PhasePrinter.printPhaseStackWithException(stack, printer, e);

        printer.trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().isVerbose()) {
            PhasePrinter.printedExceptionsForState.add(context.state);
        }
    }

    static void printUnprocessedPhaseContextObjects(final PhaseStack stack, final IPhaseState<?> state, final PhaseContext<?> context) {
        PhasePrinter.printMessageWithCaughtException(stack, "Failed to process all PhaseContext captured!",
                "During the processing of a phase, certain objects were captured in a PhaseContext. All of them should have been removed from the PhaseContext by this point",
                state, context, null);
    }

    static void printRunawayPhase(final PhaseStack stack, final IPhaseState<?> state, final PhaseContext<?> context) {
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().isVerbose() && !PhasePrinter.hasPrintedAboutRunnawayPhases) {
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
        printer.trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().isVerbose() && PhasePrinter.printRunawayCount++ > SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().getMaximumRunawayCount()) {
            PhasePrinter.hasPrintedAboutRunnawayPhases = true;
        }
    }

    static void printRunnawayPhaseCompletion(final PhaseStack stack, final IPhaseState<?> state) {
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().isVerbose() && !PhasePrinter.hasPrintedAboutRunnawayPhases) {
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
        PhasePrinter.printPhaseStackWithException(stack, printer, new Exception("RunawayPhase"));
        printer.trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().isVerbose() && PhasePrinter.printRunawayCount++ > 3) {
            PhasePrinter.hasPrintedAboutRunnawayPhases = true;
        }
    }

    public static void generateVersionInfo(final PrettyPrinter printer) {
        for (final PluginContainer pluginContainer : SpongeImpl.getInternalPlugins()) {
            pluginContainer.getVersion().ifPresent(version ->
                    printer.add("%s : %s", pluginContainer.getName(), version)
            );
        }
    }

    static void printIncorrectPhaseCompletion(final PhaseStack stack, final IPhaseState<?> prevState, final IPhaseState<?> state) {
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().isVerbose() && !PhasePrinter.completedIncorrectStates.isEmpty()) {
            for (final Tuple<IPhaseState<?>, IPhaseState<?>> tuple : PhasePrinter.completedIncorrectStates) {
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
        PhasePrinter.printPhaseStackWithException(stack, printer, new Exception("Incorrect Phase Completion"));
        printer.trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().isVerbose()) {
            PhasePrinter.completedIncorrectStates.add(new Tuple<>(prevState, state));
        }
    }

    static void printEmptyStackOnCompletion(final PhaseContext<?> context) {
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
        printer.trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().isVerbose()) {
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
        printer.trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
    }

    static void printAsyncBlockChange(final ServerWorldBridge mixinWorld, final BlockPos pos, final net.minecraft.block.BlockState newState) {
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
            .log(SpongeImpl.getLogger(), Level.ERROR);
    }

    public static void printAsyncEntitySpawn(final Entity entity) {
        // We aren't in the server thread at this point, and an entity is spawning on the server....
        // We will DEFINITELY be doing bad things otherwise. We need to artificially capture here.
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().captureEntitiesAsync()) {
            // Print a pretty warning about not capturing an async spawned entity, but don't care about spawning.
            if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().isVerbose()) {
                return;
            }
            // Just checking if we've already printed once about it.
            // If we have, we don't want to print any more times.
            if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().verboseErrors() && PhasePrinter.hasPrintedAsyncEntities) {
                return;
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
            PhasePrinter.hasPrintedAsyncEntities = true;
            return;
        }
        PhaseTracker.ASYNC_CAPTURED_ENTITIES.add((net.minecraft.entity.Entity) entity);
        // At this point we can print an exception about it, if we are told to.
        // Print a pretty warning about not capturing an async spawned entity, but don't care about spawning.
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().isVerbose()) {
            return;
        }
        // Just checking if we've already printed once about it.
        // If we have, we don't want to print any more times.
        if (!SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().verboseErrors() && PhasePrinter.hasPrintedAsyncEntities) {
            return;
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
        PhasePrinter.hasPrintedAsyncEntities = true;
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

        if (!trackerConfig.isVerbose() && PhasePrinter.printedExceptionForMaximumProcessDepth.contains(state)) {
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
