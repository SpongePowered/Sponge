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
package org.spongepowered.common.event.tracking.context.transaction;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;

final class ReverseDeepIterator implements Iterator<GameTransaction<@NonNull ?>> {

    private GameTransaction<@NonNull ?> parent;
    private boolean hasScannedParent = false;
    private Iterator<GameTransaction<@NonNull ?>> child;
    private @Nullable GameTransaction<@NonNull ?> previous;

    ReverseDeepIterator(final GameTransaction<@NonNull ?> pointer) {
        this.parent = pointer;
        this.child = pointer.reverseChildIterator();
        if (this.child.hasNext()) {
            this.previous = this.child.next();
        }
        if (this.previous == null) {
            this.previous = pointer;
        }
    }

    @Override
    public boolean hasNext() {
        if (this.previous != null) {
            return true;
        }
        if (this.child.hasNext()) {
            this.previous = this.child.next();
            return true;
        }
        if (!this.hasScannedParent) {
            this.previous = this.parent;
            this.hasScannedParent = true;
            return true;
        }
        if (this.parent.previous != null) {
            this.hasScannedParent = false;
            final GameTransaction<@NonNull ?> previous = this.parent.previous;
            this.parent = previous;
            this.child = previous.reverseChildIterator();
            if (this.child.hasNext()) {
                this.previous = this.child.next();
            } else {
                this.previous = previous;
            }
            return true;
        }

        return false;
    }

    @Override
    public GameTransaction<@NonNull ?> next() {
        if (this.previous != null) {
            final GameTransaction<@NonNull ?> next = this.previous;
            this.previous = null;
            return next;
        }
        // Basically, someone's not calling hasNext, so we call it for them
        if (this.hasNext()) {
            return this.next();
        }
        throw new NoSuchElementException("No GameTransaction left to go to");
    }
}
