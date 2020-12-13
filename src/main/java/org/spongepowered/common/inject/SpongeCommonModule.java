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
import com.google.inject.binder.AnnotatedBindingBuilder;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.manager.CommandManager;
import org.spongepowered.api.config.ConfigManager;
import org.spongepowered.api.data.DataManager;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.network.channel.ChannelRegistry;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.registry.GameRegistry;
import org.spongepowered.api.service.ServiceProvider;
import org.spongepowered.api.sql.SqlManager;
import org.spongepowered.api.util.metric.MetricsConfigManager;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeGame;
import org.spongepowered.common.SpongePlatform;
import org.spongepowered.common.command.manager.SpongeCommandManager;
import org.spongepowered.common.config.PluginConfigManager;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.event.SpongeEventManager;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.network.channel.ChannelBufferAllocator;
import org.spongepowered.common.network.channel.SpongeChannelRegistry;
import org.spongepowered.common.registry.SpongeGameRegistry;
import org.spongepowered.common.service.game.SpongeGameScopedServiceProvider;
import org.spongepowered.common.sql.SpongeSqlManager;
import org.spongepowered.common.util.metric.SpongeMetricsConfigManager;

import javax.annotation.OverridingMethodsMustInvokeSuper;

public final class SpongeCommonModule extends PrivateModule {

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void configure() {
        this.bind(Logger.class).toInstance(SpongeCommon.getLogger());
        this.bindAndExpose(Game.class).to(SpongeGame.class);
        this.bindAndExpose(Platform.class).to(SpongePlatform.class);
        this.bindAndExpose(MinecraftVersion.class).toInstance(SpongeCommon.MINECRAFT_VERSION);
        this.bindAndExpose(ChannelRegistry.class).toInstance(new SpongeChannelRegistry(ChannelBufferAllocator.POOLED));
        this.bindAndExpose(EventManager.class).to(SpongeEventManager.class);
        this.bindAndExpose(PluginManager.class).toInstance(Launch.getInstance().getPluginManager());
        this.bindAndExpose(GameRegistry.class).to(SpongeGameRegistry.class);
        this.bindAndExpose(DataManager.class).to(SpongeDataManager.class);
        this.bindAndExpose(ConfigManager.class).to(PluginConfigManager.class);
        this.bindAndExpose(MetricsConfigManager.class).to(SpongeMetricsConfigManager.class);
        this.bindAndExpose(SqlManager.class).to(SpongeSqlManager.class);
        this.bindAndExpose(ServiceProvider.GameScoped.class).to(SpongeGameScopedServiceProvider.class);
        this.bindAndExpose(CommandManager.class).to(SpongeCommandManager.class);

        this.requestStaticInjection(SpongeCommon.class);
        this.requestStaticInjection(Sponge.class);
    }

    protected <T> AnnotatedBindingBuilder<T> bindAndExpose(final Class<T> type) {
        this.expose(type);
        return this.bind(type);
    }
}
