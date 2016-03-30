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

import org.spongepowered.api.event.Event;
import org.spongepowered.common.SpongeImpl;

import java.util.function.Consumer;

import javax.annotation.Nullable;

public final class EventConsumer<E extends Event> {

    public static <E extends Event> EventConsumer<E> event(E event) {
        return new EventConsumer<>(event);
    }

    @Nullable private Consumer<E> cancelledConsumer;
    @Nullable private Consumer<E> nonCancelledConsumer;
    @Nullable private Consumer<E> postConsumer;
    private final E event;

    private EventConsumer(E event) {
        this.event = event;
    }

    public EventConsumer<E> cancelled(Consumer<E> consumer) {
        this.cancelledConsumer = consumer;
        return this;
    }

    public EventConsumer<E> nonCancelled(Consumer<E> consumer) {
        this.nonCancelledConsumer = consumer;
        return this;
    }

    public EventConsumer<E> post(Consumer<E> consumer) {
        this.postConsumer = consumer;
        return this;
    }


    @Nullable
    public Consumer<E> getCancelledConsumer() {
        return this.cancelledConsumer;
    }

    @Nullable
    public Consumer<E> getNonCancelledConsumer() {
        return this.nonCancelledConsumer;
    }

    @Nullable
    public Consumer<E> getPostConsumer() {
        return this.postConsumer;
    }

    public E getEvent() {
        return this.event;
    }

    public E process() {
        final E event = this.getEvent();
        if (SpongeImpl.postEvent(event)) {
            if (this.getCancelledConsumer() != null) {
                this.getCancelledConsumer().accept(event);
            }
        } else {
            if (this.getNonCancelledConsumer() != null) {
                this.getNonCancelledConsumer().accept(event);
            }
        }
        if (this.getPostConsumer() != null) {
            this.getPostConsumer().accept(event);
        }
        return event;
    }
}
