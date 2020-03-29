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
package org.spongepowered.common.event.listener;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.After;
import org.spongepowered.api.event.filter.cause.Before;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.common.event.EventFilterTest;

public class BeforeAfterCauseListener {

    public boolean beforeCauseCalled;
    public boolean afterCauseCalled;

    public boolean beforeCauseCalledInc;
    public boolean beforeCauseCalledEx;
    public boolean afterCauseCalledInc;
    public boolean afterCauseCalledEx;

    @Listener
    public void beforeCauseListener(EventFilterTest.SubEvent event, @Before(Extent.class) Player player) {
        this.beforeCauseCalled = true;
    }

    @Listener
    public void afterCauseListener(EventFilterTest.SubEvent event, @After(BlockState.class) Entity entity) {
        this.afterCauseCalled = true;
    }

    @Listener
    public void beforeCauseListenerInclude(EventFilterTest.SubEvent event, @Before(value = Extent.class, typeFilter = Player.class) Player player) {
        this.beforeCauseCalledInc = true;
    }

    @Listener
    public void beforeCauseListenerExclude(EventFilterTest.SubEvent event, @Before(value = Extent.class, typeFilter = Player.class, inverse = true) Player player) {
        this.beforeCauseCalledEx = true;
    }

    @Listener
    public void afterCauseListenerInclude(EventFilterTest.SubEvent event, @After(value = BlockState.class, typeFilter = Player.class) Entity entity) {
        this.afterCauseCalledInc = true;
    }

    @Listener
    public void afterCauseListenerExclude(EventFilterTest.SubEvent event, @After(value = BlockState.class, typeFilter = Player.class, inverse = true) Entity entity) {
        this.afterCauseCalledEx = true;
    }
}
