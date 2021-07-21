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

import java.lang.reflect.Type;
import java.util.Objects;

public final class SpongeEventListenerRegistration<T extends Event> implements EventListenerRegistration<T> {

    private final Type eventType;
    private final PluginContainer plugin;
    private final Order order;
    private final boolean beforeModifications;
    private final EventListener<? super T> listener;

    private SpongeEventListenerRegistration(final BuilderImpl<T> builder) {
        this.eventType = builder.eventType;
        this.plugin = builder.plugin;
        this.order = builder.order;
        this.beforeModifications = builder.beforeModifications;
        this.listener = builder.listener;
    }

    @Override
    public Type eventType() {
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
    public EventListener<? super T> listener() {
        return this.listener;
    }

    public static final class FactoryImpl implements EventListenerRegistration.Factory {

        @Override
        public <T extends Event> Builder<T> builder(final TypeToken<T> eventType) {
            return new BuilderImpl<>(Objects.requireNonNull(eventType, "eventType"));
        }
    }

    public static final class BuilderImpl<T extends Event> implements EventListenerRegistration.Builder<T> {

        final Type eventType;
        PluginContainer plugin;
        Order order;
        boolean beforeModifications;
        EventListener<? super T> listener;

        private BuilderImpl(final TypeToken<T> eventType) {
            this.eventType = eventType.getType();
        }

        @Override
        public Builder<T> plugin(final PluginContainer plugin) {
            this.plugin = Objects.requireNonNull(plugin, "plugin");
            return this;
        }

        @Override
        public Builder<T> order(final Order order) {
            this.order = order;
            return this;
        }

        @Override
        public Builder<T> beforeModifications(final boolean beforeModifications) {
            this.beforeModifications = beforeModifications;
            return this;
        }

        @Override
        public Builder<T> listener(final EventListener<? super T> listener) {
            this.listener = Objects.requireNonNull(listener, "listener");
            return this;
        }

        @Override
        public Builder<T> reset() {
            this.plugin = null;
            this.order = Order.DEFAULT;
            this.beforeModifications = false;
            this.listener = null;
            return this;
        }

        @Override
        public EventListenerRegistration<T> build() {
            Objects.requireNonNull(this.plugin, "plugin");
            Objects.requireNonNull(this.listener, "listener");

            return new SpongeEventListenerRegistration<>(this);
        }
    }
}
