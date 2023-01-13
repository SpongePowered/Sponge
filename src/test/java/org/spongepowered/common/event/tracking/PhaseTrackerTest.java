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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.common.launch.SpongeExtension;

@ExtendWith(SpongeExtension.class)
public class PhaseTrackerTest {

    @Test
    public void testPoppingFramePopsCauses() {
        final PhaseTracker phaseTracker = PhaseTracker.getInstance();

        // We start by pushing a frame...
        CauseStackManager.StackFrame frame1 = phaseTracker.pushCauseFrame();
        phaseTracker.pushCause(1);
        phaseTracker.pushCause(2);

        // Then we push another frame
        CauseStackManager.StackFrame frame = phaseTracker.pushCauseFrame();
        phaseTracker.pushCause(3);

        assertEquals(3, phaseTracker.currentCause().root());
        assertEquals(3, phaseTracker.currentCause().all().size());

        // Now pop the frame
        phaseTracker.popCauseFrame(frame);
        assertEquals(2, phaseTracker.currentCause().root());
        assertEquals(2, phaseTracker.currentCause().all().size());

        // Remove for next tests.
        phaseTracker.popCauseFrame(frame1);
    }

    @Test
    public void testPoppingFrameRemovesFrameContexts() {
        final PhaseTracker phaseTracker = PhaseTracker.getInstance();

        String cmd1 = "one";
        String cmd2 = "two";

        // We start by pushing a frame...
        CauseStackManager.StackFrame frame1 = phaseTracker.pushCauseFrame();
        phaseTracker.pushCause(1);
        phaseTracker.addContext(EventContextKeys.COMMAND, cmd1);

        // Then we push another frame
        CauseStackManager.StackFrame frame = phaseTracker.pushCauseFrame();
        phaseTracker.addContext(EventContextKeys.COMMAND, cmd2);

        assertEquals(cmd2, phaseTracker.context(EventContextKeys.COMMAND).get());

        // Now pop the frame
        phaseTracker.popCauseFrame(frame);
        assertEquals(cmd1, phaseTracker.context(EventContextKeys.COMMAND).get());

        phaseTracker.popCauseFrame(frame1);
        assertFalse(phaseTracker.context(EventContextKeys.COMMAND).isPresent());
    }

}
