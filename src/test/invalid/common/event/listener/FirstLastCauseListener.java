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

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Last;
import org.spongepowered.common.event.EventFilterTest;

public class FirstLastCauseListener {

    public boolean firstCauseCalled;
    public boolean lastCauseCalled;

    public boolean firstCauseCalledInc;
    public boolean firstCauseCalledEx;
    public boolean lastCauseCalledInc;
    public boolean lastCauseCalledEx;

    @Listener
    public void firstCauseListener(EventFilterTest.SubEvent event, @First Player player) {
        this.firstCauseCalled = true;
    }

    @Listener
    public void lastCauseListener(EventFilterTest.SubEvent event, @Last Player player) {
        this.lastCauseCalled = true;
    }

    @Listener
    public void firstCauseListenerInclude(EventFilterTest.SubEvent event, @First(typeFilter = Player.class) Player player) {
        this.firstCauseCalledInc = true;
    }

    @Listener
    public void firstCauseListenerExclude(EventFilterTest.SubEvent event, @First(typeFilter = Player.class, inverse = true) Player player) {
        this.firstCauseCalledEx = true;
    }

    @Listener
    public void lastCauseListenerInclude(EventFilterTest.SubEvent event, @Last(typeFilter = Player.class) Entity entity) {
        this.lastCauseCalledInc = true;
    }

    @Listener
    public void lastCauseListenerExclude(EventFilterTest.SubEvent event, @Last(typeFilter = Player.class, inverse = true) Entity entity) {
        this.lastCauseCalledEx = true;
    }

}
