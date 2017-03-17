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

import org.apache.logging.log4j.Level;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.tracking.phase.general.GeneralPhase;

import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

public abstract class BaseCauseTracker {
    protected static final BiConsumer<PrettyPrinter, PhaseContext> CONTEXT_PRINTER = (printer, context) ->
            context.forEach(namedCause -> {
                        printer.add("        - Name: %s", namedCause.getName());
                        printer.addWrapped(100, "          Object: %s", namedCause.getCauseObject());
                    }
            );

    protected static final BiConsumer<PrettyPrinter, PhaseData> PHASE_PRINTER = (printer, data) -> {
        printer.add("  - Phase: %s", data.state);
        printer.add("    Context:");
        data.context.forEach(namedCause -> {
            printer.add("    - Name: %s", namedCause.getName());
            final Object causeObject = namedCause.getCauseObject();
            if (causeObject instanceof PhaseContext) {
                CONTEXT_PRINTER.accept(printer, (PhaseContext) causeObject);
            } else {
                printer.addWrapped(100, "      Object: %s", causeObject);
            }
        });
    };

    protected final CauseStack stack = new CauseStack();

    public final boolean isVerbose = SpongeImpl.getGlobalConfig().getConfig().getCauseTracker().isVerbose();
    public final boolean verboseErrors = SpongeImpl.getGlobalConfig().getConfig().getCauseTracker().verboseErrors();

    public void switchToPhase(IPhaseState state, PhaseContext phaseContext) {
        checkNotNull(state, "State cannot be null!");
        checkNotNull(state.getPhase(), "Phase cannot be null!");
        checkNotNull(phaseContext, "PhaseContext cannot be null!");
        checkArgument(phaseContext.isComplete(), "PhaseContext must be complete!");
        final IPhaseState currentState = this.stack.peek().state;
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
     * This method pushes a new phase onto the stack, runs phaseBody,
     * and calls completePhase afterwards.
     *
     * <p>This method ensures that the necessary cleanup is performed if
     * an exception is thrown by phaseBody - i.e. logging a message,
     * and calling completePhase</p>
     * @param state
     * @param context
     * @param phaseBody
     */
    public void switchToPhase(IPhaseState state, PhaseContext context, Callable<Void> phaseBody) {
        this.switchToPhase(state, context);
        try {
            phaseBody.call();
        } catch (Exception e) {
            this.abortCurrentPhase(e);
            return;
        }
        this.completePhase(state);
    }

    /**
     * Used when exception occurs during the main body of a phase.
     * Avoids running the normal unwinding code
     */
    public void abortCurrentPhase(Exception e) {
        PhaseData data = this.stack.peek();
        this.printMessageWithCaughtException("Exception during phase body", "Something happened trying to run the main body of a phase", data.state, data.context, e);

        // Since an exception occured during the main phase code, we don't know what state we're in.
        // Therefore, we skip running the normal unwind functions that completePhase calls,
        // and simply op the phase from the stack.
        popFromError();
    }

    public void completePhase(IPhaseState prevState) {
        final PhaseData currentPhaseData = this.stack.peek();
        final IPhaseState state = currentPhaseData.state;
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
            popFromError();

        }

        if (this.isVerbose && this.stack.size() > 6 && state != GeneralPhase.Post.UNWINDING && !state.isExpectedForReEntrance()) {
            // This printing is to detect possibilities of a phase not being cleared properly
            // and resulting in a "runaway" phase state accumulation.
            printRunnawayPhaseCompletion(state);
        }
        this.stack.pop();
        doCompletePhase(currentPhaseData);
    }

    protected void popFromError() {
        this.stack.pop();
    }

    protected abstract void doCompletePhase(PhaseData currentPhaseData);

    private void printRunnawayPhaseCompletion(IPhaseState state) {
        final PrettyPrinter printer = new PrettyPrinter(60);
        printer.add("Completing Phase").centre().hr();
        printer.addWrapped(50, "Detecting a runaway phase! Potentially a problem where something isn't completing a phase!!!");
        printer.add("  %s : %s", "Affected Tracker", this.getTrackerDescription());
        printer.add();
        printer.addWrapped(60, "%s : %s", "Completing phase", state);
        printer.add(" Phases Remaining:");
        this.stack.forEach(data -> PHASE_PRINTER.accept(printer, data));
        printer.add("Stacktrace:");
        printer.add(new Exception("Stack trace"));
        printer.add();
        generateVersionInfo(printer);
        printer.trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
    }

    protected abstract String getTrackerDescription();

    private void generateVersionInfo(PrettyPrinter printer) {
        for (PluginContainer pluginContainer : SpongeImpl.getInternalPlugins()) {
            pluginContainer.getVersion().ifPresent(version ->
                    printer.add("%s : %s", pluginContainer.getName(), version)
            );
        }
    }

    private void printIncorrectPhaseCompletion(IPhaseState prevState, IPhaseState state) {
        PrettyPrinter printer = new PrettyPrinter(60).add("Completing incorrect phase").centre().hr()
                .addWrapped(50, "Sponge's tracking system is very dependent on knowing when"
                        + "a change to any world takes place, however, we are attempting"
                        + "to complete a \"phase\" other than the one we most recently entered."
                        + "This is an error usually on Sponge's part, so a report"
                        + "is required on the issue tracker on GitHub.").hr()
                .add("  %s : %s", "Affected Tracker", this.getTrackerDescription())
                .add("Expected to exit phase: %s", prevState)
                .add("But instead found phase: %s", state)
                .add("StackTrace:")
                .add(new Exception());
        printer.add(" Phases Remaining:");
        this.stack.forEach(data -> PHASE_PRINTER.accept(printer, data));
        printer.add();
        generateVersionInfo(printer);
        printer.trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
    }

    private void printEmptyStackOnCompletion() {
        final PrettyPrinter printer = new PrettyPrinter(60).add("Unexpected ").centre().hr()
                .addWrapped(50, "Sponge's tracking system is very dependent on knowing when"
                        + "a change to any world takes place, however, we have been told"
                        + "to complete a \"phase\" without having entered any phases."
                        + "This is an error usually on Sponge's part, so a report"
                        + "is required on the issue tracker on GitHub.").hr()
                .add("  %s : %s", "Affected Tracker", this.getTrackerDescription())
                .add("StackTrace:")
                .add(new Exception())
                .add();
        generateVersionInfo(printer);
        printer.trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
    }

    private void printRunawayPhase(IPhaseState state, PhaseContext context) {
        final PrettyPrinter printer = new PrettyPrinter(40);
        printer.add("Switching Phase").centre().hr();
        printer.addWrapped(50, "Detecting a runaway phase! Potentially a problem where something isn't completing a phase!!!");
        printer.add("  %s : %s", "Affected Tracker", this.getTrackerDescription());
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
    }

    private void printPhaseIncompatibility(IPhaseState currentState, IPhaseState incompatibleState) {
        PrettyPrinter printer = new PrettyPrinter(80);
        printer.add("Switching Phase").centre().hr();
        printer.add("Phase incompatibility detected! Attempting to switch to an invalid phase!");
        printer.add("  %s : %s", "Affected Tracker", this.getTrackerDescription());
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
    }

    public void printMessageWithCaughtException(String header, String subHeader, Exception e) {
        this.printMessageWithCaughtException(header, subHeader, this.getCurrentState(), this.getCurrentContext(), e);
    }

    public void printMessageWithCaughtException(String header, String subHeader, IPhaseState state, PhaseContext context, Exception e) {
        final PrettyPrinter printer = new PrettyPrinter(40);
        printer.add(header).centre().hr()
                .add("%s %s", subHeader, state)
                .addWrapped(40, "%s :", "PhaseContext");
        printer.add("  %s : %s", "Affected Tracker", this.getTrackerDescription());
        CONTEXT_PRINTER.accept(printer, context);
        printer.addWrapped(60, "%s :", "Phases remaining");
        this.stack.forEach(data -> PHASE_PRINTER.accept(printer, data));
        printer.add("Stacktrace:")
                .add(e);
        printer.add();
        generateVersionInfo(printer);
        printer.trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
    }

    public PhaseData getCurrentPhaseData() {
        return this.stack.peek();
    }

    public IPhaseState getCurrentState() {
        return this.stack.peekState();
    }

    public PhaseContext getCurrentContext() {
        return this.stack.peekContext();
    }
}
