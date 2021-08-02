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
package org.spongepowered.common.event.tracking;

import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class PooledPhaseState<C extends PhaseContext<C>> implements IPhaseState<C> {

    // @formatter: off
    private @Nullable transient C serverCached;
    private @Nullable C clientCached;
    // @formatter: on

    protected PooledPhaseState() {
    }

    @Override
    public final C createPhaseContext(final PhaseTracker tracker) {
        if (Thread.currentThread() != tracker.getSidedThread()) {
            throw new IllegalStateException("Asynchronous Thread Access to PhaseTracker: " + tracker);
        }

        if (tracker == PhaseTracker.SERVER) {
            if (this.serverCached != null && !this.serverCached.isCompleted) {
                final C cached = this.serverCached;
                this.serverCached = null;
                return cached;
            }
        } else if (tracker == PhaseTracker.CLIENT) {
            if (this.clientCached != null && !this.clientCached.isCompleted) {
                final C cached = this.clientCached;
                this.clientCached = null;
                return cached;
            }
        }
        final @Nullable C peek = tracker.getContextPoolFor(this).pollFirst();
        if (peek != null) {
            if (tracker == PhaseTracker.SERVER) {
                this.serverCached = peek;
            } else if (tracker == PhaseTracker.CLIENT) {
                this.clientCached = peek;
            }
            return peek;
        }
        final C maybeCached = this.createNewContext(tracker);
        if (tracker == PhaseTracker.SERVER) {
            this.serverCached = maybeCached;
        } else if (tracker == PhaseTracker.CLIENT) {
            this.clientCached = maybeCached;
        }
        return maybeCached;
    }

    final void releaseContextFromPool(final C context) {
        final PhaseTracker createdTracker = context.createdTracker;
        if (Thread.currentThread() != createdTracker.getSidedThread()) {
            throw new IllegalStateException("Asynchronous Thread Access to PhaseTracker: " + createdTracker);
        }
        if (createdTracker == PhaseTracker.SERVER) {
            if (this.serverCached == context) {
                return;
            }
            if (this.serverCached == null) {
                this.serverCached = context;
                return;
            }
        }
        if (createdTracker == PhaseTracker.CLIENT) {
            if (this.clientCached == context) {
                return;
            }
            if (this.clientCached == null) {
                this.clientCached = context;
                return;
            }
        }
        createdTracker.getContextPoolFor(this).push(context);

    }

    protected abstract C createNewContext(PhaseTracker tracker);

}
