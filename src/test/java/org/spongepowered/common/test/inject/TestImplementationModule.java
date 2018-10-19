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
package org.spongepowered.common.test.inject;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.spongepowered.api.Platform;
import org.spongepowered.api.Server;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.network.ChannelRegistrar;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.common.SpongeGame;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongePlatform;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.inject.SpongeImplementationModule;
import org.spongepowered.common.test.TestGame;
import org.spongepowered.common.test.TestServer;

import java.util.Optional;

public class TestImplementationModule extends SpongeImplementationModule {

    @Override
    protected void configure() {
        super.configure();

        this.bind(Server.class).to(TestServer.class);
        this.bind(SpongeGame.class).to(TestGame.class);
        Platform platform = mock(Platform.class);
        when(platform.getExecutionType()).thenReturn(Platform.Type.SERVER);
        PluginContainer mock = mock(PluginContainer.class);
        when(platform.getContainer(any())).thenReturn(mock);
        this.bind(Platform.class).toInstance(platform);

        PluginManager manager = mock(PluginManager.class);
        when(mock.getId()).thenReturn("sponge");
        when(manager.getPlugin(anyString())).thenReturn(Optional.of(mock));
        when(manager.fromInstance(any())).thenReturn(Optional.of(mock));
        this.bind(PluginManager.class).toInstance(manager);
        PluginContainer common = mock(PluginContainer.class);
        SpongeImpl.setSpongePlugin(common);
        this.bind(EventManager.class).toInstance(mock(EventManager.class));
        this.bind(ChannelRegistrar.class).toInstance(mock(ChannelRegistrar.class));
    }
}
