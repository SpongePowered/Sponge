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
package org.spongepowered.common.event.tracking.phase;

import org.spongepowered.common.event.tracking.IPhaseState;

import java.util.ArrayDeque;
import java.util.Deque;

import javax.annotation.Nullable;

public class TrackingPhases {

    private static final int DEFAULT_QUEUE_SIZE = 16;

    public static final WorldPhase WORLD = new WorldPhase(TrackingPhases.GENERAL).addChild(TrackingPhases.SPAWNING).addChild(TrackingPhases.BLOCK);
    public static final SpawningPhase SPAWNING = new SpawningPhase(TrackingPhases.GENERAL);
    public static final BlockPhase BLOCK    = new BlockPhase(TrackingPhases.GENERAL);
    public static final GeneralPhase GENERAL  = new GeneralPhase(null).addChild(TrackingPhases.SPAWNING).addChild(TrackingPhases.BLOCK);
    public static final PacketPhase PACKET = new PacketPhase(TrackingPhases.GENERAL);

    private final Deque<IPhaseState> states = new ArrayDeque<>(DEFAULT_QUEUE_SIZE);

    public void push(IPhaseState state) {
        this.states.push(state);
    }

    public IPhaseState pop() {
        return this.states.pop();
    }

    @Nullable
    public IPhaseState peek() {
        return this.states.peek();
    }

    public TrackingPhase current() {
        IPhaseState current = this.states.peek();
        return current == null ? null : current.getPhase();
    }

    public void gotoState(TrackingPhase phase, IPhaseState state) {

    }

    public void validateEmpty() {
        if (!this.states.isEmpty()) {
            System.err.printf("******* Exception!!! The phases should be empty!***********%n");
            System.err.printf("The current phases: %s%n", this.states);
            throw new IllegalStateException("The states are not empty!!");
        }
    }

}