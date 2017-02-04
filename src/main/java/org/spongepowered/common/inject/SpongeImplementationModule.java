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
package org.spongepowered.common.inject;

import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.api.Platform;
import org.spongepowered.api.asset.AssetManager;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.network.ChannelRegistrar;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.service.SimpleServiceManager;
import org.spongepowered.api.world.TeleportHelper;
import org.spongepowered.common.SpongeBootstrap;
import org.spongepowered.common.SpongeGame;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.asset.SpongeAssetManager;
import org.spongepowered.common.command.SpongeCommandDisambiguator;
import org.spongepowered.common.command.SpongeCommandManager;
import org.spongepowered.common.profile.SpongeProfileManager;
import org.spongepowered.common.registry.SpongeGameRegistry;
import org.spongepowered.common.scheduler.SpongeScheduler;
import org.spongepowered.common.scheduler.SpongeTaskBuilder;
import org.spongepowered.common.world.SpongeTeleportHelper;
import org.spongepowered.common.world.WorldManager;
import org.spongepowered.common.world.pregen.SpongeChunkPreGenerateTask;
import org.spongepowered.common.world.storage.WorldStorageUtil;

import javax.annotation.OverridingMethodsMustInvokeSuper;

public class SpongeImplementationModule extends PrivateModule {

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void configure() {
        this.bind(SpongeImpl.class);

        this.bind(Game.class).to(SpongeGame.class);
        this.expose(Game.class);
        this.bind(MinecraftVersion.class).toInstance(SpongeImpl.MINECRAFT_VERSION);
        this.expose(MinecraftVersion.class);
        this.bind(ServiceManager.class).to(SimpleServiceManager.class);
        this.expose(ServiceManager.class);
        this.bind(AssetManager.class).to(SpongeAssetManager.class);
        this.expose(AssetManager.class);
        this.bind(GameRegistry.class).to(SpongeGameRegistry.class);
        this.expose(GameRegistry.class);
        this.bind(TeleportHelper.class).to(SpongeTeleportHelper.class);
        this.expose(TeleportHelper.class);
        this.bind(Scheduler.class).to(SpongeScheduler.class);
        this.expose(Scheduler.class);
        this.bind(CommandManager.class).to(SpongeCommandManager.class);
        this.expose(CommandManager.class);

        // These are bound in implementation-specific modules
        this.expose(Platform.class);
        this.expose(PluginManager.class);
        this.expose(EventManager.class);
        this.expose(ChannelRegistrar.class);

        this.bind(Logger.class).toInstance(SpongeImpl.getSlf4jLogger());
        this.bind(org.apache.logging.log4j.Logger.class).toInstance(SpongeImpl.getLogger());

        this.requestStaticInjection(SpongeImpl.class);
        this.requestStaticInjection(SpongeBootstrap.class);
        this.requestStaticInjection(SpongeTaskBuilder.class);
        this.requestStaticInjection(SpongeProfileManager.class);
        this.requestStaticInjection(SpongeChunkPreGenerateTask.class);
        this.requestStaticInjection(WorldManager.class);
        this.requestStaticInjection(WorldStorageUtil.class);
    }

    @Provides
    @Singleton
    SpongeCommandManager commandManager(final Logger logger, final Game game) {
        return new SpongeCommandManager(logger, new SpongeCommandDisambiguator(game));
    }

}
