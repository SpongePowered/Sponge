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

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.Engine;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.common.SpongeLifecycle;
import org.spongepowered.common.event.lifecycle.StartedEngineEventImpl;
import org.spongepowered.common.event.lifecycle.StartingEngineEventImpl;
import org.spongepowered.common.event.lifecycle.StoppingEngineEventImpl;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.launch.plugin.DummyPluginContainer;
import org.spongepowered.plugin.PluginContainer;

import java.util.Collection;
import java.util.stream.Collectors;

public final class VanillaLifecycle extends SpongeLifecycle {

    public VanillaLifecycle(final Engine engine) {
        super(engine);
    }

    public void registerPluginListeners() {
        for (final PluginContainer plugin : this.filterInternalPlugins(this.engine.getGame().getPluginManager().getPlugins())) {
            this.engine.getGame().getEventManager().registerListeners(plugin, plugin.getInstance());
        }
    }

    // Methods are in order of the SpongeCommon lifecycle

    public void callConstructEvent() {
        for (final PluginContainer plugin : this.filterInternalPlugins(this.engine.getGame().getPluginManager().getPlugins())) {
            this.engine.getGame().getEventManager().post(SpongeEventFactory.createConstructPluginEvent(this.engine.getCauseStackManager()
                .getCurrentCause(), this.engine.getGame(), plugin));
        }
    }

    public void callStartingEngineEvent() {
        this.engine.getGame().getEventManager().post(new StartingEngineEventImpl<>(PhaseTracker.getCauseStackManager().getCurrentCause(), (TypeToken<Engine>) TypeToken.of(this.engine.getClass()), this.engine.getGame(), this.engine));
    }

    public void callStartedEngineEvent() {
        this.engine.getGame().getEventManager().post(new StartedEngineEventImpl<>(PhaseTracker.getCauseStackManager().getCurrentCause(), (TypeToken<Engine>) TypeToken.of(this.engine.getClass()), this.engine.getGame(), this.engine));
    }

    public void callLoadedGameEvent() {
        this.engine.getGame().getEventManager().post(SpongeEventFactory.createLoadedGameEvent(PhaseTracker.getCauseStackManager().getCurrentCause(), this.engine.getGame()));
    }

    public void callStoppingEngineEvent() {
        this.engine.getGame().getEventManager().post(new StoppingEngineEventImpl<>(PhaseTracker.getCauseStackManager().getCurrentCause(), (TypeToken<Engine>) TypeToken.of(this.engine.getClass()), this.engine.getGame(), this.engine));
    }

    private Collection<PluginContainer> filterInternalPlugins(final Collection<PluginContainer> plugins) {
        return plugins
            .stream()
            .filter(plugin -> !(plugin instanceof DummyPluginContainer))
            .collect(Collectors.toList());
    }
}
