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
import com.google.inject.binder.AnnotatedBindingBuilder;
import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.AssetManager;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.config.ConfigManager;
import org.spongepowered.api.data.DataManager;
import org.spongepowered.api.data.property.PropertyRegistry;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.network.ChannelRegistrar;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.service.SimpleServiceManager;
import org.spongepowered.api.util.metric.MetricsConfigManager;
import org.spongepowered.api.world.TeleportHelper;
import org.spongepowered.common.SpongeBootstrap;
import org.spongepowered.common.SpongeGame;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.asset.SpongeAssetManager;
import org.spongepowered.common.command.SpongeCommandDisambiguator;
import org.spongepowered.common.command.SpongeCommandManager;
import org.spongepowered.common.config.SpongeConfigManager;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.property.SpongePropertyRegistry;
import org.spongepowered.common.event.SpongeCauseStackManager;
import org.spongepowered.common.registry.SpongeGameRegistry;
import org.spongepowered.common.scheduler.SpongeScheduler;
import org.spongepowered.common.util.metric.SpongeMetricsConfigManager;
import org.spongepowered.common.world.teleport.SpongeTeleportHelper;

import javax.annotation.OverridingMethodsMustInvokeSuper;

public class SpongeImplementationModule extends PrivateModule {

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void configure() {
        //noinspection UninstantiableBinding
        this.bindAndExpose(Game.class).to(SpongeGame.class);
        this.bindAndExpose(MinecraftVersion.class).toInstance(SpongeImpl.MINECRAFT_VERSION);
        this.bindAndExpose(ServiceManager.class).to(SimpleServiceManager.class);
        this.bindAndExpose(AssetManager.class).to(SpongeAssetManager.class);
        this.bindAndExpose(GameRegistry.class).to(SpongeGameRegistry.class);
        this.bindAndExpose(TeleportHelper.class).to(SpongeTeleportHelper.class);
        this.bindAndExpose(Scheduler.class).to(SpongeScheduler.class);
        this.bindAndExpose(CommandManager.class).to(SpongeCommandManager.class);
        this.bindAndExpose(DataManager.class).to(SpongeDataManager.class);
        this.bindAndExpose(ConfigManager.class).to(SpongeConfigManager.class);
        this.bindAndExpose(PropertyRegistry.class).to(SpongePropertyRegistry.class);
        this.bindAndExpose(CauseStackManager.class).to(SpongeCauseStackManager.class);
        this.bindAndExpose(MetricsConfigManager.class).to(SpongeMetricsConfigManager.class);

        // These are bound in implementation-specific modules
        this.expose(Platform.class);
        this.expose(PluginManager.class);
        this.expose(EventManager.class);
        this.expose(ChannelRegistrar.class);

        this.bind(Logger.class).toInstance(SpongeImpl.getLogger());
        this.bind(org.slf4j.Logger.class).toInstance(LoggerFactory.getLogger(SpongeImpl.getLogger().getName()));

        this.requestStaticInjection(SpongeImpl.class);
        this.requestStaticInjection(Sponge.class);
        this.requestStaticInjection(SpongeBootstrap.class);
    }

    protected <T> AnnotatedBindingBuilder<T> bindAndExpose(final Class<T> type) {
        this.expose(type);
        return this.bind(type);
    }

    @Provides
    @Singleton
    SpongeCommandManager commandManager(final Logger logger, final Game game) {
        return new SpongeCommandManager(logger, new SpongeCommandDisambiguator(game));
    }

}
