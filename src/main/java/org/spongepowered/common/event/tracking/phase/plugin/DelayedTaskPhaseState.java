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
package org.spongepowered.common.event.tracking.phase.plugin;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.util.PrettyPrinter;
import org.spongepowered.plugin.PluginContainer;

import java.util.function.BiConsumer;

class DelayedTaskPhaseState extends PluginPhaseState<DelayedTaskPhaseState.Context> {

    private final BiConsumer<CauseStackManager.StackFrame, Context> PLUGIN_MODIFIER = super.getFrameModifier()
        .andThen((frame, context) -> {
            context.getSource(Object.class).ifPresent(frame::pushCause);
            if (context.container != null) {
                frame.pushCause(context.container);
            }
            if (context.delayedContextPopulator != null) {
                context.delayedContextPopulator.accept(context, frame);
            }
        });

    @Override
    public Context createNewContext(final PhaseTracker tracker) {
        return new Context(this, tracker);
    }

    @Override
    public BiConsumer<CauseStackManager.StackFrame, Context> getFrameModifier() {
        return this.PLUGIN_MODIFIER;
    }

    @Override
    public void unwind(final Context phaseContext) {

    }

    public static class Context extends PluginPhaseContext<Context> {

        @Nullable PluginContainer container;
        @Nullable BiConsumer<PhaseContext<@NonNull ?>, CauseStackManager.StackFrame> delayedContextPopulator;

        public Context(final IPhaseState<Context> phaseState, final PhaseTracker tracker) {
            super(phaseState, tracker);
        }

        public Context container(final PluginContainer container) {
            this.container = container;
            return this;
        }

        public Context setDelayedContextPopulator(
            final @Nullable BiConsumer<PhaseContext<@NonNull ?>, CauseStackManager.StackFrame> delayedContextPopulator
        ) {
            this.delayedContextPopulator = delayedContextPopulator;
            return this;
        }

        @Override
        public PrettyPrinter printCustom(final PrettyPrinter printer, final int indent) {
            super.printCustom(printer, indent);
            final String s = String.format("%1$" + indent + "s", "");
            if (this.container != null) {
                printer.add(s + "- %s: %s", "PluginContainer", this.container);
            }
            return printer;
        }

        @Override
        protected void reset() {
            super.reset();
            this.container = null;
        }
    }
}
