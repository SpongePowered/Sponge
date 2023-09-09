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

import net.minecraftforge.eventbus.BusBuilderImpl;
import net.minecraftforge.eventbus.EventBus;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBusInvokeDispatcher;
import net.minecraftforge.eventbus.api.IEventExceptionHandler;
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
    private static Field busIDField;
    private static Field exceptionHandlerField;
    private static Field trackPhasesField;
    private static Field shutdownField;
    private static Field baseTypeField;
    private static Field checkTypesOnDispatchField;

    static {
        try {
            SpongeEventBus.busIDField = EventBus.class.getDeclaredField("busID");
            SpongeEventBus.exceptionHandlerField = EventBus.class.getDeclaredField("exceptionHandler");
            SpongeEventBus.trackPhasesField = EventBus.class.getDeclaredField("trackPhases");
            SpongeEventBus.shutdownField = EventBus.class.getDeclaredField("shutdown");
            SpongeEventBus.baseTypeField = EventBus.class.getDeclaredField("baseType");
            SpongeEventBus.checkTypesOnDispatchField = EventBus.class.getDeclaredField("checkTypesOnDispatch");

        } catch (final Exception ex) {
            // Burn this to the ground
            throw new RuntimeException(ex);
        }
    }

    // reflected fields that are stored again to prevent multiple reflection calls
    private final int rbusID;
    private final IEventExceptionHandler rexceptionHandler;
    private final boolean rtrackPhases;
    private boolean rshutdown;
    private final Class<?> rbaseType;
    private final boolean rcheckTypesOnDispatch;

    public SpongeEventBus(final BusBuilderImpl busBuilder) {
        super(busBuilder);

        // Sponge Start - I hope ya'll still like reflection
        try {
            SpongeEventBus.busIDField.setAccessible(true);
            SpongeEventBus.exceptionHandlerField.setAccessible(true);
            SpongeEventBus.trackPhasesField.setAccessible(true);
            SpongeEventBus.shutdownField.setAccessible(true);
            SpongeEventBus.baseTypeField.setAccessible(true);
            SpongeEventBus.checkTypesOnDispatchField.setAccessible(true);

            this.rbusID = SpongeEventBus.busIDField.getInt(this);
            this.rexceptionHandler = (IEventExceptionHandler) SpongeEventBus.exceptionHandlerField.get(this);
            this.rtrackPhases = SpongeEventBus.trackPhasesField.getBoolean(this);
            this.rshutdown = SpongeEventBus.shutdownField.getBoolean(this);
            this.rbaseType = (Class<?>) SpongeEventBus.baseTypeField.get(this);
            this.rcheckTypesOnDispatch = SpongeEventBus.checkTypesOnDispatchField.getBoolean(this);

            SpongeEventBus.busIDField.setAccessible(false);
            SpongeEventBus.exceptionHandlerField.setAccessible(false);
            SpongeEventBus.trackPhasesField.setAccessible(false);
            SpongeEventBus.shutdownField.setAccessible(false);
            SpongeEventBus.baseTypeField.setAccessible(false);
            SpongeEventBus.checkTypesOnDispatchField.setAccessible(false);
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
        if (this.rcheckTypesOnDispatch && !rbaseType.isInstance(event))
        {
            throw new IllegalArgumentException("Cannot post event of type " + event.getClass().getSimpleName() + " to this event. Must match type: " + this.rbaseType.getSimpleName());
        }

        IEventListener[] listeners = event.getListenerList().getListeners(this.rbusID);
        int index = 0;
        for (; index < listeners.length; index++)
        {
            final IEventListener listener = listeners[index];
            if (!this.rtrackPhases && Objects.equals(listener.getClass(), EventPriority.class)) continue;

            try (
                final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame();
                final PhaseContext<@NonNull ?> context = SpongeEventManager.createListenerContext(null))
            {
                if (context != null) {
                    context.buildAndSwitch();
                }

                wrapper.invoke(listener, event);
            } catch (final Throwable t) {
                this.rexceptionHandler.handleException(this, event, listeners, index, t);
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
