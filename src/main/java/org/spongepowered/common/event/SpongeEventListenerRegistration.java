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

import io.leangen.geantyref.TypeToken;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.EventListenerRegistration;
import org.spongepowered.api.event.Order;
import org.spongepowered.plugin.PluginContainer;

import java.util.Objects;

public final class SpongeEventListenerRegistration<E extends Event> implements EventListenerRegistration<E> {

    private final TypeToken<E> eventType;
    private final PluginContainer plugin;
    private final Order order;
    private final boolean beforeModifications;
    private final EventListener<? super E> listener;

    private SpongeEventListenerRegistration(final BuilderImpl<E> builder) {
        this.eventType = builder.eventType;
        this.plugin = builder.plugin;
        this.order = builder.order;
        this.beforeModifications = builder.beforeModifications;
        this.listener = builder.listener;
    }

    @Override
    public TypeToken<E> eventType() {
        return this.eventType;
    }

    @Override
    public PluginContainer plugin() {
        return this.plugin;
    }

    @Override
    public Order order() {
        return this.order;
    }

    @Override
    public boolean beforeModifications() {
        return this.beforeModifications;
    }

    @Override
    public EventListener<? super E> listener() {
        return this.listener;
    }

    public static final class FactoryImpl implements EventListenerRegistration.Factory {

        @Override
        public <E extends Event> Builder<E> builder(final TypeToken<E> eventType) {
            return new BuilderImpl<>(Objects.requireNonNull(eventType, "eventType"));
        }
    }

    public static final class BuilderImpl<E extends Event> implements EventListenerRegistration.Builder<E> {

        final TypeToken<E> eventType;
        PluginContainer plugin;
        Order order;
        boolean beforeModifications;
        EventListener<? super E> listener;

        private BuilderImpl(final TypeToken<E> eventType) {
            this.eventType = eventType;
        }

        @Override
        public Builder<E> plugin(final PluginContainer plugin) {
            this.plugin = Objects.requireNonNull(plugin, "plugin");
            return this;
        }

        @Override
        public Builder<E> order(final Order order) {
            this.order = order;
            return this;
        }

        @Override
        public Builder<E> beforeModifications(final boolean beforeModifications) {
            this.beforeModifications = beforeModifications;
            return this;
        }

        @Override
        public Builder<E> listener(final EventListener<? super E> listener) {
            this.listener = Objects.requireNonNull(listener, "listener");
            return this;
        }

        @Override
        public Builder<E> reset() {
            this.plugin = null;
            this.order = Order.DEFAULT;
            this.beforeModifications = false;
            this.listener = null;
            return this;
        }

        @Override
        public EventListenerRegistration<E> build() {
            Objects.requireNonNull(this.plugin, "plugin");
            Objects.requireNonNull(this.listener, "listener");

            return new SpongeEventListenerRegistration<>(this);
        }
    }
}
