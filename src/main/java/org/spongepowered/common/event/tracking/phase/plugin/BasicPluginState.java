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

import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.common.event.tracking.PhaseTracker;

import java.util.function.BiConsumer;

public class BasicPluginState extends PluginPhaseState<BasicPluginContext> {

    private final BiConsumer<CauseStackManager.StackFrame, BasicPluginContext> PLUGIN_MODIFIER = super.getFrameModifier()
        .andThen((frame, context) -> {
            context.getSource(Object.class).ifPresent(frame::pushCause);
            if (context.container != null) {
                frame.pushCause(context.container);
            }
        });

    @Override
    public BasicPluginContext createNewContext(final PhaseTracker tracker) {
        return new BasicPluginContext(this, tracker);
    }

    @Override
    public BiConsumer<CauseStackManager.StackFrame, BasicPluginContext> getFrameModifier() {
        return this.PLUGIN_MODIFIER;
    }

    @Override
    public void unwind(final BasicPluginContext phaseContext) {

    }
}
