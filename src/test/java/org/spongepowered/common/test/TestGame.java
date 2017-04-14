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
package org.spongepowered.common.test;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.spongepowered.api.Client;
import org.spongepowered.api.GameDictionary;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Server;
import org.spongepowered.api.asset.AssetManager;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.network.ChannelRegistrar;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.world.TeleportHelper;
import org.spongepowered.common.SpongeGame;
import org.spongepowered.common.registry.SpongeGameRegistry;

import java.nio.file.Path;
import java.util.NoSuchElementException;

@Singleton
public class TestGame extends SpongeGame {

    @Inject
    public TestGame(Platform platform, PluginManager pluginManager, EventManager eventManager, AssetManager assetManager,
            ServiceManager serviceManager, TeleportHelper teleportHelper, ChannelRegistrar channelRegistrar, SpongeGameRegistry gameRegistry,
            Scheduler scheduler, CommandManager commandManager) {
        super(platform, pluginManager, eventManager, assetManager, serviceManager, teleportHelper, channelRegistrar, gameRegistry, scheduler,
                commandManager);
    }

    @Override
    public boolean isServerAvailable() {
        return false;
    }

    @Override
    public Server getServer() {
        throw new NoSuchElementException();
    }

    @Override public boolean isClientAvailable() {
        return false;
    }

    @Override public Client getClient() {
        throw new NoSuchElementException();
    }

    @Override
    public GameDictionary getGameDictionary() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path getSavesDirectory() {
        throw new UnsupportedOperationException();
    }

}
