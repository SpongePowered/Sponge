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
package org.spongepowered.neoforge.launch.event;

import net.neoforged.bus.BusBuilderImpl;
import net.neoforged.bus.EventBus;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.common.event.manager.SpongeEventManager;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;

public final class SpongeEventBus extends EventBus {

    public SpongeEventBus(final BusBuilderImpl busBuilder) {
        super(busBuilder);
    }

    @Override
    public <T extends Event> T post(T event) {
        try (
            final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame();
            final PhaseContext<@NonNull ?> context = SpongeEventManager.createListenerContext(null)
        ) {
            if (context != null) {
                context.buildAndSwitch();
            }
            return super.post(event);
        }
    }

    @Override
    public <T extends Event> T post(EventPriority phase, T event) {
        try (
            final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame();
            final PhaseContext<@NonNull ?> context = SpongeEventManager.createListenerContext(null)
        ) {
            if (context != null) {
                context.buildAndSwitch();
            }
            return super.post(phase, event);
        }
    }
}
