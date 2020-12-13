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
package org.spongepowered.common.bridge.util.concurrent;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.common.event.tracking.PhaseContext;

import java.util.Optional;
import java.util.function.BiConsumer;

public interface TrackedTickDelayedTaskBridge {

    /**
     * This is a handy method to pass a long an uncompleted,
     * un-marked context to swap in when the task is executed.
     * Effectively allows for cross thread context switches whereby
     * an asynchronous process can pass along pertinent information
     * to the delayed task to {@link PhaseContext#buildAndSwitch()}
     * when the task is appropriately ran.
     *
     * @param context The context of data to associate with the task
     */
    void bridge$contextShift(BiConsumer<PhaseContext<@NonNull ?>, CauseStackManager.StackFrame> context);

    Optional<BiConsumer<PhaseContext<@NonNull ?>, CauseStackManager.StackFrame>> bridge$getFrameModifier();


}
