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
package org.spongepowered.vanilla.launch.event;

import static com.google.common.base.Preconditions.checkNotNull;

import co.aikar.timings.Timing;

import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.Order;
import co.aikar.timings.sponge.SpongeTimings;
import org.spongepowered.common.event.SpongeEventListener;
import org.spongepowered.plugin.PluginContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

public final class RegisteredListener<T extends Event> implements SpongeEventListener<T>, Comparable<RegisteredListener<?>> {

    private final PluginContainer plugin;

    private final EventType<T> eventType;
    private final Order order;

    private final EventListener<? super T> listener;

    private final boolean beforeModifications;
    private Timing listenerTimer;

    RegisteredListener(PluginContainer plugin, EventType<T> eventType, Order order, EventListener<? super T> listener, boolean beforeModifications) {
        this.plugin = checkNotNull(plugin, "plugin");
        this.eventType = checkNotNull(eventType, "eventType");
        this.order = checkNotNull(order, "order");
        this.listener = checkNotNull(listener, "listener");
        this.beforeModifications = beforeModifications;
    }

    public PluginContainer getPlugin() {
        return this.plugin;
    }

    public EventType<T> getEventType() {
        return this.eventType;
    }

    public Order getOrder() {
        return this.order;
    }

    public boolean isBeforeModifications() {
        return this.beforeModifications;
    }

    public Timing getTimingsHandler() {
        if (this.listenerTimer == null) {
            this.listenerTimer = SpongeTimings.getPluginTimings(this.plugin, this.getHandle().getClass().getSimpleName());
        }
        return this.listenerTimer;
    }

    @Override
    public Object getHandle() {
        if (this.listener instanceof SpongeEventListener) {
            return ((SpongeEventListener<?>) this.listener).getHandle();
        }

        return this.listener;
    }

    @Override
    public void handle(T event) throws Exception {
        this.listener.handle(event);
    }

    @Override
    public int compareTo(RegisteredListener<?> handler) {
        return this.order.compareTo(handler.order);
    }

    public static final class Cache {

        private final List<RegisteredListener<?>> listeners;
        private final EnumMap<Order, List<RegisteredListener<?>>> listenersByOrder;

        Cache(List<RegisteredListener<?>> listeners) {
            this.listeners = listeners;

            this.listenersByOrder = new EnumMap<>(Order.class);
            for (RegisteredListener<?> handler : listeners) {
                final List<RegisteredListener<?>> list = this.listenersByOrder.computeIfAbsent(handler.getOrder(), order -> new ArrayList<>());
                list.add(handler);
            }
        }

        public List<RegisteredListener<?>> getListeners() {
            return this.listeners;
        }

        public List<RegisteredListener<?>> getListenersByOrder(Order order) {
            final List<RegisteredListener<?>> list = this.listenersByOrder.get(checkNotNull(order, "order"));
            if (list == null) {
                return Collections.emptyList();
            }
            return list;
        }

    }

}
