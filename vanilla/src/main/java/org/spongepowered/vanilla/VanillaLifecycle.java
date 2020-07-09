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
package org.spongepowered.vanilla;

import com.google.inject.Inject;
import org.spongepowered.api.Engine;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.service.ServiceProvider;
import org.spongepowered.common.SpongeLifecycle;
import org.spongepowered.common.event.SpongeEventManager;
import org.spongepowered.common.service.SpongeServiceProvider;
import org.spongepowered.plugin.PluginContainer;

public final class VanillaLifecycle extends SpongeLifecycle {

    @Inject
    public VanillaLifecycle(Game game, Engine engine, EventManager eventManager, PluginManager pluginManager, ServiceProvider serviceProvider) {
        super(game, engine, eventManager, pluginManager, serviceProvider);
    }

    public void registerPluginListeners() {
        for (final PluginContainer plugin : this.pluginManager.getPlugins()) {
            this.eventManager.registerListeners(plugin, plugin.getInstance());
        }
    }

    public void callConstructEventToPlugins() {
        for (final PluginContainer plugin : this.pluginManager.getPlugins()) {
            this.eventManager.post(SpongeEventFactory.createConstructPluginEvent(this.engine.getCauseStackManager().getCurrentCause(), this.game, plugin));
        }
    }
}
