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
package org.spongepowered.forge.launch.event;

import net.minecraftforge.eventbus.EventBus;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBusInvokeDispatcher;
import net.minecraftforge.eventbus.api.IEventListener;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.common.event.manager.SpongeEventManager;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;

import java.lang.reflect.Field;
import java.util.Objects;

public final class SpongeEventBus extends EventBus {

    // I hope ya'll like reflection...
    private static Field checkTypesOnDispatchField;
    private static Field busIDField;

    static {
        try {
            SpongeEventBus.checkTypesOnDispatchField = EventBus.class.getDeclaredField("checkTypesOnDispatch");
            SpongeEventBus.busIDField = EventBus.class.getDeclaredField("busID");
        } catch (final Exception ex) {
            // Burn this to the ground
            throw new RuntimeException(ex);
        }
    }

    private final BusBuilder builder;

    // reflected fields that are stored again to prevent multiple reflection calls
    private final boolean rcheckTypesOnDispatch;
    private final int rbusID;
    private boolean rshutdown;

    public SpongeEventBus(final BusBuilder busBuilder) {
        super(busBuilder);
        this.builder = busBuilder;

        // Sponge Start - I hope ya'll still like reflection
        this.rshutdown = busBuilder.isStartingShutdown();
        try {
            SpongeEventBus.checkTypesOnDispatchField.setAccessible(true);
            SpongeEventBus.busIDField.setAccessible(true);

            this.rcheckTypesOnDispatch = SpongeEventBus.checkTypesOnDispatchField.getBoolean(null);
            this.rbusID = SpongeEventBus.busIDField.getInt(this);

            SpongeEventBus.checkTypesOnDispatchField.setAccessible(false);
            SpongeEventBus.busIDField.setAccessible(false);
        } catch (final Exception ex) {
            // Burn this to the ground again
            throw new RuntimeException(ex);
        }
        // Sponge End
    }

    @Override
    public boolean post(final Event event, final IEventBusInvokeDispatcher wrapper) {
        // @formatter:off

        // Sponge - use the builder/member fields. Avoids reflection but remains very hacky...
        if (this.rshutdown) return false;
        if (this.rcheckTypesOnDispatch && !this.builder.getMarkerType().isInstance(event))
        {
            throw new IllegalArgumentException("Cannot post event of type " + event.getClass().getSimpleName() + " to this event. Must match type: " + this.builder.getMarkerType().getSimpleName());
        }

        IEventListener[] listeners = event.getListenerList().getListeners(this.rbusID);
        int index = 0;
        for (; index < listeners.length; index++)
        {
            final IEventListener listener = listeners[index];
            if (!this.builder.getTrackPhases() && Objects.equals(listener.getClass(), EventPriority.class)) continue;

            try (
                final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame();
                final PhaseContext<@NonNull ?> context = SpongeEventManager.createListenerContext(null))
            {
                if (context != null) {
                    context.buildAndSwitch();
                }

                wrapper.invoke(listener, event);
            } catch (final Throwable t) {
                this.builder.getExceptionHandler().handleException(this, event, listeners, index, t);
            }
        }
        return event.isCancelable() && event.isCanceled();

        // @formatter:on
    }

    @Override
    public void shutdown() {
        super.shutdown();
        this.rshutdown = true;
    }

    @Override
    public void start() {
        super.start();
        this.rshutdown = false;
    }
}
