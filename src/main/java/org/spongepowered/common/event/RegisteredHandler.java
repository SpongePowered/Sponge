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
package org.spongepowered.common.event;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventHandler;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.EnumMap;
import java.util.List;

public final class RegisteredHandler<T extends Event> implements SpongeEventHandler<T>, Comparable<RegisteredHandler<?>> {

    private final PluginContainer plugin;

    private final Class<T> eventClass;
    private final Order order;

    private final EventHandler<? super T> handler;

    private final boolean ignoreCancelled;
    private final boolean beforeModifications;

    RegisteredHandler(PluginContainer plugin, Class<T> eventClass, Order order, EventHandler<? super T> handler, boolean ignoreCancelled, boolean beforeModifications) {
        this.plugin = checkNotNull(plugin, "plugin");
        this.eventClass = checkNotNull(eventClass, "eventClass");
        this.order = checkNotNull(order, "order");
        this.handler = checkNotNull(handler, "handler");
        this.ignoreCancelled = ignoreCancelled;
        this.beforeModifications = beforeModifications;
    }

    public PluginContainer getPlugin() {
        return this.plugin;
    }

    public Class<T> getEventClass() {
        return this.eventClass;
    }

    public Order getOrder() {
        return this.order;
    }

    public boolean isBeforeModifications() {
        return this.beforeModifications;
    }

    @Override
    public Object getHandle() {
        if (this.handler instanceof SpongeEventHandler) {
            return ((SpongeEventHandler<?>) this.handler).getHandle();
        }

        return this.handler;
    }

    @Override
    public void handle(T event) throws Exception {
        if (this.ignoreCancelled && event instanceof Cancellable && ((Cancellable) event).isCancelled()) {
            return;
        }

        this.handler.handle(event);
    }

    @Override
    public int compareTo(RegisteredHandler<?> handler) {
        return this.order.compareTo(handler.order);
    }

    public static final class Cache {

        private final List<RegisteredHandler<?>> handlers;
        private final EnumMap<Order, List<RegisteredHandler<?>>> handlersByOrder;

        private static final Order[] ORDERS = Order.values();

        Cache(List<RegisteredHandler<?>> handlers) {
            this.handlers = handlers;

            this.handlersByOrder = Maps.newEnumMap(Order.class);
            for (Order order : ORDERS) {
                this.handlersByOrder.put(order, Lists.<RegisteredHandler<?>>newArrayList());
            }
            for (RegisteredHandler<?> handler : handlers) {
                this.handlersByOrder.get(handler.getOrder()).add(handler);
            }
        }

        public List<RegisteredHandler<?>> getHandlers() {
            return this.handlers;
        }

        public List<RegisteredHandler<?>> getHandlersByOrder(Order order) {
            return this.handlersByOrder.get(checkNotNull(order, "order"));
        }

    }

}
