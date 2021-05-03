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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.common.bridge.server.TickTaskBridge;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.plugin.PluginContainer;

import java.util.Objects;

/**
 * Used for tasks scheduled with both the Sponge scheduler, and the built-in 'scheduled task' system in MinecraftServer
 */
public final class ScheduledTaskPhaseState extends BasicPluginState {

    @Override
    public void unwind(final BasicPluginContext phaseContext) {
        // TODO - Determine if we need to pass the supplier or perform some parameterized
        //  process if not empty method on the capture object.
        TrackingUtil.processBlockCaptures(phaseContext);
    }

    @Override
    public void foldContextForThread(
            final BasicPluginContext context,
            final TickTaskBridge returnValue
    ) {
        final @Nullable Object source = context.getSource();
        final PluginContainer plugin = Objects.requireNonNull(context.container, "Scheduled Task has a null plugin!");

        returnValue.bridge$contextShift(((context1, stackFrame) -> {
            if (source != null) {
                stackFrame.pushCause(source);
            }
            stackFrame.pushCause(plugin);
        }));
    }
}
