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
package org.spongepowered.server.inject;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Server;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.network.ChannelRegistrar;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.common.SpongeGame;
import org.spongepowered.common.SpongePlatform;
import org.spongepowered.common.event.SpongeEventManager;
import org.spongepowered.common.inject.SpongeImplementationModule;
import org.spongepowered.server.VanillaGame;
import org.spongepowered.server.network.VanillaChannelRegistrar;
import org.spongepowered.server.plugin.MetadataContainer;
import org.spongepowered.server.plugin.VanillaPluginManager;

public class SpongeVanillaModule extends SpongeImplementationModule {

    private final MinecraftServer server;
    private final MetadataContainer metadata;

    public SpongeVanillaModule(MinecraftServer server, MetadataContainer metadata) {
        this.server = server;
        this.metadata = metadata;
    }

    @Override
    @SuppressWarnings("UnnecessaryStaticInjection") // we're injecting into mixins >:)
    protected void configure() {
        super.configure();

        this.bind(SpongeGame.class).to(VanillaGame.class);
        this.bind(Platform.class).to(SpongePlatform.class);
        this.bind(PluginManager.class).to(VanillaPluginManager.class);
        this.bind(EventManager.class).to(SpongeEventManager.class);
        this.bind(ChannelRegistrar.class).to(VanillaChannelRegistrar.class);

        this.bind(Server.class).toInstance((Server) this.server);
        this.bind(MetadataContainer.class).toInstance(this.metadata);

        this.requestStaticInjection(DedicatedServer.class);
    }
}
