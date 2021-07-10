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
package org.spongepowered.common.event.tracking.phase.generation;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.TickNextTickData;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.common.bridge.world.TickNextTickDataBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;

import java.util.Objects;
import java.util.function.BiConsumer;

final class DeferredScheduledUpdatePhaseState extends GeneralGenerationPhaseState<DeferredScheduledUpdatePhaseState.Context> {

    private final BiConsumer<CauseStackManager.StackFrame, Context> CHUNK_LOAD_MODIFIER =
        super.getFrameModifier().andThen((frame, context) -> {
            frame.pushCause(context.getEntry());
        });

    public DeferredScheduledUpdatePhaseState() {
        super("CHUNK_LOAD");
    }

    @Override
    public Context createNewContext(final PhaseTracker tracker) {
        return new Context(tracker);
    }

    @Override
    public BiConsumer<CauseStackManager.StackFrame, Context> getFrameModifier() {
        return this.CHUNK_LOAD_MODIFIER;
    }

    @Override
    public void associateScheduledTickUpdate(
        final Context asContext, ServerLevel level, final TickNextTickData<?> entry
    ) {
    }

    public static final class Context extends GenerationContext<Context> {

        private TickNextTickData<?> entry;

        Context(final PhaseTracker tracker) {
            super(GenerationPhase.State.DEFERRED_SCHEDULED_UPDATE, tracker);
        }

        public Context scheduledUpdate(final TickNextTickData<?> entry) {
            this.entry = entry;
            return this;
        }

        public TickNextTickData<?> getEntry() {
            return Objects.requireNonNull(this.entry, "NextTickListEntry was not initialized");
        }
    }
}
