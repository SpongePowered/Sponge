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

final class DeepIterator implements Iterator<GameTransaction<@NonNull ?>> {

    private GameTransaction<@NonNull ?> parent;
    private Iterator<GameTransaction<@NonNull ?>> child;
    private @Nullable GameTransaction<@NonNull ?> next;

    DeepIterator(final GameTransaction<@NonNull ?> pointer) {
        this.parent = pointer;
        this.child = pointer.childIterator();
        this.next = pointer;
    }

    @Override
    public boolean hasNext() {
        if (this.next != null) {
            return true;
        }
        if (this.child.hasNext()) {
            this.next = this.child.next();
            return true;
        }
        if (this.parent.next != null) {
            final GameTransaction<@NonNull ?> next = this.parent.next;
            this.parent = next;
            this.child = next.childIterator();
            this.next = next;
            return true;
        }

        return false;
    }

    @Override
    public GameTransaction<@NonNull ?> next() {
        if (this.next != null) {
            final GameTransaction<@NonNull ?> next = this.next;
            this.next = null;
            return next;
        }
        // Basically, someone's not calling hasNext, so we call it for them
        if (this.hasNext()) {
            return this.next();
        }
        throw new NoSuchElementException("No GameTransaction left to go to");
    }
}
