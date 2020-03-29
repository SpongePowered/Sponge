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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.lwts.runner.LaunchWrapperTestRunner;

@RunWith(LaunchWrapperTestRunner.class)
public class CauseStackManagerTest {

    @Test
    public void testPoppingFramePopsCauses() throws Exception {
        final SpongeCauseStackManager causeStackManager = SpongeImpl.getCauseStackManager();

        // We start by pushing a frame...
        SpongeCauseStackManager.StackFrame frame1 = causeStackManager.pushCauseFrame();
        causeStackManager.pushCause(1);
        causeStackManager.pushCause(2);

        // Then we push another frame
        SpongeCauseStackManager.StackFrame frame = causeStackManager.pushCauseFrame();
        causeStackManager.pushCause(3);

        Assert.assertEquals(3, causeStackManager.getCurrentCause().root());
        Assert.assertEquals(3, causeStackManager.getCurrentCause().all().size());

        // Now pop the frame
        causeStackManager.popCauseFrame(frame);
        Assert.assertEquals(2, causeStackManager.getCurrentCause().root());
        Assert.assertEquals(2, causeStackManager.getCurrentCause().all().size());

        // Remove for next tests.
        causeStackManager.popCauseFrame(frame1);
    }

    @Test
    public void testPoppingFrameRemovesFrameContexts() throws Exception {
        final SpongeCauseStackManager causeStackManager = SpongeImpl.getCauseStackManager();

        User user = Mockito.mock(User.class);
        Mockito.when(user.getName()).thenReturn("first");

        User user2 = Mockito.mock(User.class);
        Mockito.when(user.getName()).thenReturn("second");

        // We start by pushing a frame...
        SpongeCauseStackManager.StackFrame frame1 = causeStackManager.pushCauseFrame();
        causeStackManager.pushCause(1);
        causeStackManager.addContext(EventContextKeys.OWNER, user);

        // Then we push another frame
        SpongeCauseStackManager.StackFrame frame = causeStackManager.pushCauseFrame();
        causeStackManager.addContext(EventContextKeys.OWNER, user2);

        Assert.assertEquals(user2, causeStackManager.getContext(EventContextKeys.OWNER).get());

        // Now pop the frame
        causeStackManager.popCauseFrame(frame);
        Assert.assertEquals(user, causeStackManager.getContext(EventContextKeys.OWNER).get());

        causeStackManager.popCauseFrame(frame1);
        Assert.assertFalse(causeStackManager.getContext(EventContextKeys.OWNER).isPresent());
    }

}
