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

import net.minecraft.world.Container;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A transfer out of a container that is in progress on a {@link PhaseTracker}, for the code that finds out where
 * the item went to hand that back to the code that started the transfer. Independent of the phase stack, so a
 * phase entered in between does not hide it.
 */
// shortcut: carries only the target container, replace with the transactional item changes once they merge up from api-12
public final class ContainerTransfer implements AutoCloseable {

    private final PhaseTracker tracker;
    private final Container source;
    private @Nullable Container target;

    ContainerTransfer(final PhaseTracker tracker, final Container source) {
        this.tracker = tracker;
        this.source = source;
    }

    public Container source() {
        return this.source;
    }

    /**
     * @return The container the item was inserted into, or {@code null} when nothing inserted it into one
     */
    public @Nullable Container target() {
        return this.target;
    }

    public void target(final Container target) {
        this.target = target;
    }

    @Override
    public void close() {
        this.tracker.popTransfer(this);
    }
}
