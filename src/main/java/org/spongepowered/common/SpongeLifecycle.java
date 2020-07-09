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
package org.spongepowered.common;

import org.spongepowered.api.Engine;
import org.spongepowered.api.Game;
import org.spongepowered.common.event.SpongeEventManager;
import org.spongepowered.common.launch.plugin.SpongePluginManager;
import org.spongepowered.common.service.SpongeServiceProvider;

public abstract class SpongeLifecycle {

    protected final Game game;
    protected final Engine engine;
    protected final SpongeEventManager eventManager;
    protected final SpongePluginManager pluginManager;
    protected final SpongeServiceProvider serviceProvider;

    public SpongeLifecycle(final Game game, final Engine engine, final SpongeEventManager eventManager, final SpongePluginManager pluginManager,
        final SpongeServiceProvider serviceProvider) {
        this.game = game;
        this.engine = engine;
        this.eventManager = eventManager;
        this.pluginManager = pluginManager;
        this.serviceProvider = serviceProvider;
    }

    public void establishServices() {
        this.serviceProvider.init();
    }
}
