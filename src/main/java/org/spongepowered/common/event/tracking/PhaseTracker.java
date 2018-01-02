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
import static com.google.common.base.Preconditions.checkState;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.phase.TrackingPhase;
import org.spongepowered.common.event.tracking.phase.block.BlockPhase;
import org.spongepowered.common.event.tracking.phase.general.GeneralPhase;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;
import org.spongepowered.common.registry.type.world.BlockChangeFlagRegistryModule;
import org.spongepowered.common.world.SpongeBlockChangeFlag;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

/**
 * A helper object that is hard attached to a {@link World} that acts as a
 * proxy object entering and processing between different states of the
 * world and its objects.
 */
@SuppressWarnings("unchecked")
public final class PhaseTracker {

    private static final CopyOnWriteArrayList<net.minecraft.entity.Entity> ASYNC_CAPTURED_ENTITIES = new CopyOnWriteArrayList<>();

    @SuppressWarnings("unused")
    public static final Task ASYNC_TO_SYNC_SPAWNER = Task.builder()
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
                frame.addContext(EventContextKeys.SPAWN_TYPE, InternalSpawnTypes.FORCED);
                for (net.minecraft.entity.Entity entity : entities) {
                    // At this point, we don't care what the causes are...
                    PhaseTracker.getInstance().spawnEntityWithCause((World) entity.getEntityWorld(), (Entity) entity);
                }
            }

        })
        .submit(SpongeImpl.getPlugin());

    static final BiConsumer<PrettyPrinter, PhaseContext<?>> CONTEXT_PRINTER = (printer, context) ->
        context.printCustom(printer);

    private static final BiConsumer<PrettyPrinter, PhaseData> PHASE_PRINTER = (printer, data) -> {
        printer.add("  - Phase: %s", data.state);
        printer.add("    Context:");
        data.context.printCustom(printer);
        /*
        data.context.getExtraContext().entrySet().forEach(namedCause -> {
            printer.add("    - Name: %s", namedCause.getKey());
            final Object causeObject = namedCause.getValue();
            if (causeObject instanceof PhaseContext) {
                CONTEXT_PRINTER.accept(printer, (PhaseContext<?>) causeObject);
            } else {
                printer.addWrapped(100, "      Object: %s", causeObject);
            }
        });
        */
    };

    private final PhaseStack stack = new PhaseStack();

    @Nullable private PhaseData currentProcessingState = null;

    public final boolean isVerbose = SpongeImpl.getGlobalConfig().getConfig().getCauseTracker().isVerbose();
    public final boolean verboseErrors = SpongeImpl.getGlobalConfig().getConfig().getCauseTracker().verboseErrors();
    public static final boolean CAPTURE_ENTITIES_ASYNC = SpongeImpl.getGlobalConfig().getConfig().getCauseTracker().captureEntitiesAsync();
    private boolean hasPrintedEmptyOnce = false;
    private boolean hasPrintedAboutRunnawayPhases = false;
    private boolean hasPrintedAsyncEntities = false;
    private List<IPhaseState<?>> printedExceptionsForBlocks = new ArrayList<>();
    private List<IPhaseState<?>> printedExceptionsForEntities = new ArrayList<>();
    private List<Tuple<IPhaseState<?>, IPhaseState<?>>> completedIncorrectStates = new ArrayList<>();
    private List<IPhaseState<?>> printedExceptionsForState = new ArrayList<>();

    private PhaseTracker() {
        // We cannot have two instances ever. ever ever.
        checkState(INSTANCE == null, "More than one PhaseTracker instance is being created!!! Two cannot exist at once!");
    }

    private static final PhaseTracker INSTANCE = new PhaseTracker();

    public static PhaseTracker getInstance() {
        return checkNotNull(INSTANCE, "PhaseTracker instance was illegally set to null!");
    }

    // ----------------- STATE ACCESS ----------------------------------

    void switchToPhase(IPhaseState<?> state, PhaseContext<?> phaseContext) {
        checkNotNull(state, "State cannot be null!");
        checkNotNull(state.getPhase(), "Phase cannot be null!");
        checkNotNull(phaseContext, "PhaseContext cannot be null!");
        checkArgument(phaseContext.isComplete(), "PhaseContext must be complete!");
        final IPhaseState<?> currentState = this.stack.peek().state;
        if (this.isVerbose) {
            if (this.stack.size() > 6 && !currentState.isExpectedForReEntrance()) {
                // This printing is to detect possibilities of a phase not being cleared properly
                // and resulting in a "runaway" phase state accumulation.
                printRunawayPhase(state, phaseContext);
            }
            if (!currentState.canSwitchTo(state) && state != GeneralPhase.Post.UNWINDING && currentState == GeneralPhase.Post.UNWINDING) {
                // This is to detect incompatible phase switches.
                printPhaseIncompatibility(currentState, state);
            }
        }

        this.stack.push(state, phaseContext);
    }

    /**
     * Used when exception occurs during the main body of a phase.
     * Avoids running the normal unwinding code
     */
    public void printExceptionFromPhase(Throwable t) {
        this.printMessageWithCaughtException("Exception during phase body", "Something happened trying to run the main body of a phase", t);

    }

    @SuppressWarnings({"rawtypes", "unused", "try"})
    public void completePhase(IPhaseState<?> prevState) {
        final PhaseData currentPhaseData = this.stack.peek();
        final IPhaseState<?> state = currentPhaseData.state;
        final boolean isEmpty = this.stack.isEmpty();
        if (isEmpty) {
            // The random occurrence that we're told to complete a phase
            // while a world is being changed unknowingly.
            printEmptyStackOnCompletion();
            return;
        }

        if (prevState != state) {
            printIncorrectPhaseCompletion(prevState, state);

            // The phase on the top of the stack was most likely never completed.
            // Since we don't know when and where completePhase was intended to be called for it,
            // we simply pop it to allow processing to continue (somewhat) as normal
            this.stack.pop();

        }

        if (this.isVerbose && this.stack.size() > 6 && state != GeneralPhase.Post.UNWINDING && !state.isExpectedForReEntrance()) {
            // This printing is to detect possibilities of a phase not being cleared properly
            // and resulting in a "runaway" phase state accumulation.
            printRunnawayPhaseCompletion(state);
        }
        this.stack.pop();
        // If pop is called, the Deque will already throw an exception if there is no element
        // so it's an error properly handled.
        final TrackingPhase phase = state.getPhase();
        final PhaseContext<?> context = currentPhaseData.context;
        try (final UnwindingPhaseContext unwinding = UnwindingPhaseContext.unwind(state, context) ) {
            // With UnwindingPhaseContext#unwind checking for post, if it is null, the try
            // will not attempt to close the phase context. If it is required,
            // it already automaticaly pushes onto the phase stack, along with
            // a new list of capture lists
            try { // Yes this is a nested try, but in the event the current phase cannot be unwound, at least unwind UNWINDING
                this.currentProcessingState = currentPhaseData;
                ((IPhaseState) state).unwind(context);
                this.currentProcessingState = null;
            } catch (Exception | NoClassDefFoundError e) {
                printMessageWithCaughtException("Exception Exiting Phase", "Something happened when trying to unwind", state, context, e);
                this.currentProcessingState = null;
            }
        } catch (Exception | NoClassDefFoundError e) {
            printMessageWithCaughtException("Exception Post Dispatching Phase", "Something happened when trying to post dispatch state", state, context, e);
            this.currentProcessingState = null;
        }
    }

    private void printRunnawayPhaseCompletion(IPhaseState<?> state) {
        if (!this.isVerbose && !this.hasPrintedAboutRunnawayPhases) {
            // Avoiding spam logs.
            return;
        }
        final PrettyPrinter printer = new PrettyPrinter(60);
        printer.add("Completing Phase").centre().hr();
        printer.addWrapped(50, "Detecting a runaway phase! Potentially a problem where something isn't completing a phase!!!");
        printer.add();
        printer.addWrapped(60, "%s : %s", "Completing phase", state);
        printer.add(" Phases Remaining:");
        this.stack.forEach(data -> PHASE_PRINTER.accept(printer, data));
        printer.add("Stacktrace:");
        printer.add(new Exception("Stack trace"));
        printer.add();
        generateVersionInfo(printer);
        printer.trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
        if (!this.isVerbose) {
            this.hasPrintedAboutRunnawayPhases = true;
        }
    }

    public void generateVersionInfo(PrettyPrinter printer) {
        for (PluginContainer pluginContainer : SpongeImpl.getInternalPlugins()) {
            pluginContainer.getVersion().ifPresent(version ->
                    printer.add("%s : %s", pluginContainer.getName(), version)
            );
        }
    }

    private void printIncorrectPhaseCompletion(IPhaseState<?> prevState, IPhaseState<?> state) {
        if (!this.isVerbose && !this.completedIncorrectStates.isEmpty()) {
            for (Tuple<IPhaseState<?>, IPhaseState<?>> tuple : this.completedIncorrectStates) {
                if ((tuple.getFirst().equals(prevState)
                        && tuple.getSecond().equals(state))) {
                    // we've already printed once about the previous state and the current state
                    // being completed incorrectly. only print it once.
                    return;
                }
            }
        }

        PrettyPrinter printer = new PrettyPrinter(60).add("Completing incorrect phase").centre().hr()
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
        this.stack.forEach(data -> PHASE_PRINTER.accept(printer, data));
        printer.add();
        generateVersionInfo(printer);
        printer.trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
        if (!this.isVerbose) {
            this.completedIncorrectStates.add(new Tuple<>(prevState, state));
        }
    }

    private void printEmptyStackOnCompletion() {
        if (!this.isVerbose && this.hasPrintedEmptyOnce) {
            // We want to only mention it once that we are completing an
            // empty state, of course something is bound to break, but
            // we don't want to spam megabytes worth of log files just
            // because of it.
            return;
        }
        final PrettyPrinter printer = new PrettyPrinter(60).add("Unexpectedly Completing An Empty Stack").centre().hr()
                .addWrapped(50, "Sponge's tracking system is very dependent on knowing when"
                                + " a change to any world takes place, however, we have been told"
                                + " to complete a \"phase\" without having entered any phases."
                                + " This is an error usually on Sponge's part, so a report"
                                + " is required on the issue tracker on GitHub.").hr()
                .add("StackTrace:")
                .add(new Exception())
                .add();
        generateVersionInfo(printer);
        printer.trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
        if (!this.isVerbose) {
            this.hasPrintedEmptyOnce = true;
        }
    }

    private void printRunawayPhase(IPhaseState<?> state, PhaseContext<?> context) {
        if (!this.isVerbose && !this.hasPrintedAboutRunnawayPhases) {
            // Avoiding spam logs.
            return;
        }
        final PrettyPrinter printer = new PrettyPrinter(40);
        printer.add("Switching Phase").centre().hr();
        printer.addWrapped(50, "Detecting a runaway phase! Potentially a problem where something isn't completing a phase!!!");
        printer.add("  %s : %s", "Entering Phase", state.getPhase());
        printer.add("  %s : %s", "Entering State", state);
        CONTEXT_PRINTER.accept(printer, context);
        printer.addWrapped(60, "%s :", "Phases remaining");
        this.stack.forEach(data -> PHASE_PRINTER.accept(printer, data));
        printer.add("  %s :", "Printing stack trace")
                .add(new Exception("Stack trace"));
        printer.add();
        generateVersionInfo(printer);
        printer.trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
        if (!this.isVerbose) {
            this.hasPrintedAboutRunnawayPhases = true;
        }
    }

    private void printPhaseIncompatibility(IPhaseState<?> currentState, IPhaseState<?> incompatibleState) {
        if (!this.isVerbose && !this.completedIncorrectStates.isEmpty()) {
            for (Tuple<IPhaseState<?>, IPhaseState<?>> tuple : this.completedIncorrectStates) {
                if ((tuple.getFirst().equals(currentState)
                        && tuple.getSecond().equals(incompatibleState))) {
                    // we've already printed once about the previous state and the current state
                    // being completed incorrectly. only print it once.
                    return;
                }
            }
        }
        PrettyPrinter printer = new PrettyPrinter(80);
        printer.add("Switching Phase").centre().hr();
        printer.add("Phase incompatibility detected! Attempting to switch to an invalid phase!");
        printer.add("  %s : %s", "Current Phase", currentState.getPhase());
        printer.add("  %s : %s", "Current State", currentState);
        printer.add("  %s : %s", "Entering incompatible Phase", incompatibleState.getPhase());
        printer.add("  %s : %s", "Entering incompatible State", incompatibleState);
        printer.add("%s :", "Current phases");
        this.stack.forEach(data -> PHASE_PRINTER.accept(printer, data));
        printer.add("  %s :", "Printing stack trace");
        printer.add(new Exception("Stack trace"));
        printer.add();
        generateVersionInfo(printer);
        printer.trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
        if (!this.isVerbose) {
            this.completedIncorrectStates.add(Tuple.of(currentState, incompatibleState));
        }
    }

    public void printMessageWithCaughtException(String header, String subHeader, Throwable e) {
        this.printMessageWithCaughtException(header, subHeader, this.getCurrentState(), this.getCurrentContext(), e);
    }

    private void printMessageWithCaughtException(String header, String subHeader, IPhaseState<?> state, PhaseContext<?> context, Throwable t) {
        final PrettyPrinter printer = new PrettyPrinter(40);
        printer.add(header).centre().hr()
                .add("%s %s", subHeader, state)
                .addWrapped(40, "%s :", "PhaseContext");
        CONTEXT_PRINTER.accept(printer, context);
        printer.addWrapped(60, "%s :", "Phases remaining");
        this.stack.forEach(data -> PHASE_PRINTER.accept(printer, data));
        printer.add("Stacktrace:")
                .add(t);
        printer.add();
        generateVersionInfo(printer);
        printer.trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
    }

    public void printExceptionFromPhase(Throwable e, PhaseContext<?> context) {
        if (!this.isVerbose && !this.printedExceptionsForState.isEmpty()) {
            for (IPhaseState<?> iPhaseState : this.printedExceptionsForState) {
                if (context.state == iPhaseState) {
                    return;
                }
            }
        }
        PrettyPrinter printer = new PrettyPrinter(60).add("Exception occurred during a PhaseState").centre().hr()
            .addWrapped("Sponge's tracking system is very dependent on NOT throwing exceptions randomly,"
                        + "sometimes it is inevitable. Unfortunately, an exception being printed now can"
                        + "be very spammy, and as a result, cause logs to reach several hundred megabytes"
                        + "in size. Since there is an exception being thrown, it is advisable to report this"
                        + "log to Sponge on GitHub.").hr()
            .add("The PhaseState having an exception: %s", context.state)
            .add("The PhaseContext:")
            ;
        printer
            .add(context.printCustom(printer));
        printer.hr()
            .add("StackTrace:")
            .add(e);
        printer.add(" Phases Remaining:");
        this.stack.forEach(data -> PHASE_PRINTER.accept(printer, data));
        printer.add();
        generateVersionInfo(printer);
        printer.trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
        if (!this.isVerbose) {
            this.printedExceptionsForState.add(context.state);
        }


    }

    private void printBlockTrackingException(PhaseData phaseData, IPhaseState<?> phaseState, Throwable e) {
        if (!this.isVerbose && !this.printedExceptionsForBlocks.isEmpty()) {
            if (this.printedExceptionsForBlocks.contains(phaseState)) {
                return;
            }
        }
        final PrettyPrinter printer = new PrettyPrinter(60).add("Exception attempting to capture a block change!").centre().hr();
        printer.addWrapped(40, "%s :", "PhaseContext");
        CONTEXT_PRINTER.accept(printer, phaseData.context);
        printer.addWrapped(60, "%s :", "Phases remaining");
        this.stack.forEach(data -> PHASE_PRINTER.accept(printer, data));
        printer.add("Stacktrace:");
        printer.add(e);
        printer.trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
        if (!this.isVerbose) {
            this.printedExceptionsForBlocks.add(phaseState);
        }
    }

    private void printUnexpectedBlockChange() {
        if (!this.isVerbose) {
            return;
        }
        new PrettyPrinter(60).add("Unexpected World Change Detected").centre().hr()
            .add("Sponge's tracking system is very dependent on knowing when\n"
                 + "a change to any world takes place, however there are chances\n"
                 + "where Sponge does not know of changes that mods may perform.\n"
                 + "In cases like this, it is best to report to Sponge to get this\n"
                 + "change tracked correctly and accurately.").hr()
            .add("StackTrace:")
            .add(new Exception())
            .trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
    }

    private void printExceptionSpawningEntity(PhaseContext<?> context, Throwable e) {
        if (!this.isVerbose && this.printedExceptionsForEntities.isEmpty()) {
            if (this.printedExceptionsForEntities.contains(context.state)) {
                return;
            }
        }
        final PrettyPrinter printer = new PrettyPrinter(60).add("Exception attempting to capture or spawn an Entity!").centre().hr();
        printer.addWrapped(40, "%s :", "PhaseContext");
        CONTEXT_PRINTER.accept(printer, context);
        printer.addWrapped(60, "%s :", "Phases remaining");
        this.stack.forEach(data -> PHASE_PRINTER.accept(printer, data));
        printer.add("Stacktrace:");
        printer.add(e);
        printer.trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
        if (!this.isVerbose) {
            this.printedExceptionsForEntities.add(context.state);
        }
    }

    String dumpStack() {
        if (this.stack.isEmpty()) {
            return "[Empty stack]";
        }

        final PrettyPrinter printer = new PrettyPrinter(40);
        this.stack.forEach(data -> PHASE_PRINTER.accept(printer, data));

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        printer.print(new PrintStream(stream));

        return stream.toString();
    }

    // ----------------- SIMPLE GETTERS --------------------------------------

    public PhaseData getCurrentPhaseData() {
        return this.stack.peek();
    }

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
     * @param mixinWorld
     * @param notifyPos The original notification position
     * @param sourceBlock The source block type
     * @param sourcePos The source block position
     */
    public void notifyBlockOfStateChange(final IMixinWorldServer mixinWorld, final BlockPos notifyPos,
        final Block sourceBlock, @Nullable final BlockPos sourcePos) {
        final IBlockState iblockstate = ((WorldServer) mixinWorld).getBlockState(notifyPos);

        try {
            // Sponge start - prepare notification
            final PhaseData peek = this.stack.peek();
            final IPhaseState<?> state = peek.state;
            ((IPhaseState) state).associateNeighborStateNotifier(peek.context,
                sourcePos, iblockstate.getBlock(), notifyPos, ((WorldServer) mixinWorld), PlayerTracker.Type.NOTIFIER);
            // Sponge End

            iblockstate.neighborChanged(((WorldServer) mixinWorld), notifyPos, sourceBlock, sourcePos);
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception while updating neighbours");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being updated");
            crashreportcategory.addDetail("Source block type", () -> {
                try {
                    return String.format("ID #%d (%s // %s)", Block.getIdFromBlock(sourceBlock),
                            sourceBlock.getUnlocalizedName(), sourceBlock.getClass().getCanonicalName());
                } catch (Throwable var2) {
                    return "ID #" + Block.getIdFromBlock(sourceBlock);
                }
            });
            CrashReportCategory.addBlockInfo(crashreportcategory, notifyPos, iblockstate);
            throw new ReportedException(crashreport);
        }
    }

    /**
     * Replacement of {@link WorldServer#setBlockState(BlockPos, IBlockState, int)}
     * that adds cause tracking.
     *
     * @param pos The position of the block state to set
     * @param newState The new state
     * @param flags The notification flags
     * @return True if the block was successfully set (or captured)
     */
    public boolean setBlockState(final IMixinWorldServer mixinWorld, final BlockPos pos, final IBlockState newState, final int flags) {
        return setBlockState(mixinWorld, pos, newState, BlockChangeFlagRegistryModule.fromNativeInt(flags));
    }

    public boolean setBlockState(final IMixinWorldServer mixinWorld, final BlockPos pos, final IBlockState newState, BlockChangeFlag flag) {
        final SpongeBlockChangeFlag spongeFlag = (SpongeBlockChangeFlag) flag;
        final net.minecraft.world.World minecraftWorld = mixinWorld.asMinecraftWorld();
        final Chunk chunk = minecraftWorld.getChunkFromBlockCoords(pos);
        // It is now possible for setBlockState to be called on an empty chunk due to our optimization
        // for returning empty chunks when we don't want a chunk to load.
        // If chunk is empty, we simply return to avoid any further logic.
        if (chunk.isEmpty()) {
            return false;
        }

        final Block block = newState.getBlock();
        // Sponge Start - Up to this point, we've copied exactly what Vanilla minecraft does.
        final IBlockState currentState = chunk.getBlockState(pos);

        if (currentState == newState) {
            // Some micro optimization in case someone is trying to set the new state to the same as current
            return false;
        }

        // Now we need to do some of our own logic to see if we need to capture.
        final PhaseData phaseData = this.stack.peek();
        final IPhaseState<?> phaseState = phaseData.state;
        final boolean isComplete = phaseState == GeneralPhase.State.COMPLETE;
        if (this.isVerbose && isComplete) {
            // The random occurrence that we're told to complete a phase
            // while a world is being changed unknowingly.
            printUnexpectedBlockChange();
        }
        if (phaseState.requiresBlockCapturing()) {
            try {
                // Default, this means we've captured the block. Keeping with the semantics
                // of the original method where true means it successfully changed.
                return TrackingUtil.trackBlockChange(this, mixinWorld, chunk, currentState, newState, pos, flag, phaseData.context, phaseState);
            } catch (Exception | NoClassDefFoundError e) {
                printBlockTrackingException(phaseData, phaseState, e);
                return false;
            }
        }
        // Sponge End - continue with vanilla mechanics
        IBlockState iblockstate = chunk.setBlockState(pos, newState);

        if (iblockstate == null) {
            return false;
        }
        // else { // Sponge - unnecessary formatting
        if (newState.getLightOpacity() != iblockstate.getLightOpacity() || newState.getLightValue() != iblockstate.getLightValue()) {
            // minecraftWorld.profiler.startSection("checkLight"); // Sponge - we don't need to us the profiler
            minecraftWorld.checkLight(pos);
            // minecraftWorld.profiler.endSection(); // Sponge - We don't need to use the profiler
        }

        if (spongeFlag.isNotifyClients() && chunk.isPopulated()) {
            minecraftWorld.notifyBlockUpdate(pos, iblockstate, newState, spongeFlag.getRawFlag());
        }

        if (spongeFlag.updateNeighbors()) {
            minecraftWorld.notifyNeighborsRespectDebug(pos, iblockstate.getBlock(), true);

            if (newState.hasComparatorInputOverride()) {
                minecraftWorld.updateComparatorOutputLevel(pos, block);
            }
        } else if (!minecraftWorld.isRemote && spongeFlag.notifyObservers()) {
            minecraftWorld.updateObservingBlocksAt(pos, block);
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
    public boolean spawnEntity(World world, Entity entity) {
        checkNotNull(entity, "Entity cannot be null!");

        // Sponge Start - handle construction phases
        if (((IMixinEntity) entity).isInConstructPhase()) {
            ((IMixinEntity) entity).firePostConstructEvents();
        }

        final net.minecraft.entity.Entity minecraftEntity = EntityUtil.toNative(entity);
        final WorldServer minecraftWorld = (WorldServer) world;
        final IMixinWorldServer mixinWorldServer = (IMixinWorldServer) minecraftWorld;
        final PhaseData phaseData = this.stack.peek();
        final IPhaseState<?> phaseState = phaseData.state;
        final PhaseContext<?> context = phaseData.context;
        final TrackingPhase phase = phaseState.getPhase();
        final boolean isForced = minecraftEntity.forceSpawn || minecraftEntity instanceof EntityPlayer;

        // Certain phases disallow entity spawns (such as block restoration)
        if (!isForced && !phaseState.allowEntitySpawns()) {
            return false;
        }

        // Sponge End - continue with vanilla mechanics
        final int chunkX = MathHelper.floor(minecraftEntity.posX / 16.0D);
        final int chunkZ = MathHelper.floor(minecraftEntity.posZ / 16.0D);

        if (!isForced && !mixinWorldServer.isMinecraftChunkLoaded(chunkX, chunkZ, true)) {
            return false;
        }
            if (minecraftEntity instanceof EntityPlayer) {
                EntityPlayer entityplayer = (EntityPlayer) minecraftEntity;
                minecraftWorld.playerEntities.add(entityplayer);
                minecraftWorld.updateAllPlayersSleepingFlag();
                SpongeImplHooks.firePlayerJoinSpawnEvent((EntityPlayerMP) entityplayer);
            } else {
                // Sponge start - check for vanilla owner
                if (minecraftEntity instanceof IEntityOwnable) {
                    IEntityOwnable ownable = (IEntityOwnable) entity;
                    net.minecraft.entity.Entity owner = ownable.getOwner();
                    if (owner != null&&owner instanceof EntityPlayer) {
                            context. owner = (User) owner;

                            entity.setCreator(ownable.getOwnerId());
                    }
                } else if (minecraftEntity instanceof EntityThrowable) {
                    EntityThrowable throwable = (EntityThrowable) minecraftEntity;
                    EntityLivingBase thrower = throwable.getThrower();
                    if (thrower != null) {
                        User user = null;
                        if (!(thrower instanceof EntityPlayer)) {
                            user = ((IMixinEntity) thrower).getCreatorUser().orElse(null);
                        } else {
                            user = (User) thrower;
                        }
                        if (user != null) {
                            context.owner = user;
                            entity.setCreator(user.getUniqueId());
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
                try {
                    return ((IPhaseState) phaseState).spawnEntityOrCapture(context, entity, chunkX, chunkZ);
                } catch (Exception | NoClassDefFoundError e) {
                    // Just in case something really happened, we should print a nice exception for people to
                    // paste us
                    printExceptionSpawningEntity(context, e);
                    return false;
                }
            }
            // Sponge end - continue on with the checks.
            minecraftWorld.getChunkFromChunkCoords(chunkX, chunkZ).addEntity(minecraftEntity);
            minecraftWorld.loadedEntityList.add(minecraftEntity);
            mixinWorldServer.onSpongeEntityAdded(minecraftEntity); // Sponge - Cannot add onEntityAdded to the access transformer because forge makes it public
            return true;

    }

    /**
     * The core implementation of {@link World#spawnEntity(Entity)} that
     * bypasses any sort of cause tracking and throws an event directly
     *
     * @param world
     * @param entity
     * @return
     */
    public boolean spawnEntityWithCause(World world, Entity entity) {
        checkNotNull(entity, "Entity cannot be null!");

        // Sponge Start - handle construction phases
        if (((IMixinEntity) entity).isInConstructPhase()) {
            ((IMixinEntity) entity).firePostConstructEvents();
        }

        final net.minecraft.entity.Entity minecraftEntity = EntityUtil.toNative(entity);
        final WorldServer worldServer = (WorldServer) world;
        final IMixinWorldServer mixinWorldServer = (IMixinWorldServer) worldServer;
        // Sponge End - continue with vanilla mechanics

        final int chunkX = MathHelper.floor(minecraftEntity.posX / 16.0D);
        final int chunkZ = MathHelper.floor(minecraftEntity.posZ / 16.0D);
        final boolean isForced = minecraftEntity.forceSpawn || minecraftEntity instanceof EntityPlayer;

        if (!isForced && !mixinWorldServer.isMinecraftChunkLoaded(chunkX, chunkZ, true)) {
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
            mixinWorldServer.forceSpawnEntity(entity);
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
     * @param mixinWorldServer The server the entity is being spawned into
     * @param entity The entity to spawn
     * @return True if the entity spawn is on the main thread.
     */
    public static boolean validateEntitySpawn(IMixinWorldServer mixinWorldServer, Entity entity) {
        if (Sponge.isServerAvailable() && Sponge.getServer().isMainThread()) {
            return true;
        }
        // We aren't in the server thread at this point, and an entity is spawning on the server....
        // We will DEFINITELY be doing bad things otherwise. We need to artificially capture here.
        if (!CAPTURE_ENTITIES_ASYNC) {
            // Print a pretty warning about not capturing an async spawned entity, but don't care about spawning.
            if (!PhaseTracker.getInstance().isVerbose) {
                return false;
            }
            // Just checking if we've already printed once about it.
            // If we have, we don't want to print any more times.
            if (!PhaseTracker.getInstance().verboseErrors && PhaseTracker.getInstance().hasPrintedAsyncEntities) {
                return false;
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
            return false;
        }
        ASYNC_CAPTURED_ENTITIES.add((net.minecraft.entity.Entity) entity);
        // At this point we can print an exception about it, if we are told to.
        // Print a pretty warning about not capturing an async spawned entity, but don't care about spawning.
        if (!PhaseTracker.getInstance().isVerbose) {
            return false;
        }
        // Just checking if we've already printed once about it.
        // If we have, we don't want to print any more times.
        if (!PhaseTracker.getInstance().verboseErrors && PhaseTracker.getInstance().hasPrintedAsyncEntities) {
            return false;
        }
        // Otherwise, let's print out either the first time, or several more times.
        new PrettyPrinter(60)
            .add("Async Entity Spawn Warning").centre().hr()
            .add("An entity was attempting to spawn off the \"main\" server thread")
            .add()
            .add("Delayed spawning is ENABLED for Sponge.")
            .add("The entity is safely captured by Sponge while off the main")
            .add("server thread, and therefor will be spawned the next tick.")
            .add("Some cases where a mod is expecting the entity back while")
            .add("async can cause issues with said mod.")
            .add()
            .add("Details of the spawn:")
            .add("%s : %s", "Entity", entity)
            .add("Stacktrace")
            .add(new Exception("Async entity spawn attempt"))
            .trace(SpongeImpl.getLogger(), Level.WARN);
        PhaseTracker.getInstance().hasPrintedAsyncEntities = true;


        return false;
    }
}
