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
package org.spongepowered.common.event.tracking.phase.tick;

public class BlockEventTickContext extends LocationBasedTickContext<BlockEventTickContext> {

    private boolean wasNotCancelled = true;
    private boolean eventSucceeded;

    BlockEventTickContext() {
        super(TickPhase.Tick.BLOCK_EVENT);
    }

    @Override
    protected void reset() {
        super.reset();
        this.wasNotCancelled = true;
        this.eventSucceeded = true;
    }

    public void setWasNotCancelled(final boolean wasNotCancelled) {
        this.wasNotCancelled = wasNotCancelled;
    }

    public boolean wasNotCancelled() {
        return this.eventSucceeded && this.wasNotCancelled;
    }

    public void setEventSucceeded(final boolean eventSucceeded) {
        this.eventSucceeded = eventSucceeded;
    }

    public boolean getEventSucceeded() {
        return this.eventSucceeded;
    }
}
