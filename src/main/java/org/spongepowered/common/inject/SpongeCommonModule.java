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

import com.google.inject.AbstractModule;
import org.spongepowered.api.Game;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.AssetManager;
import org.spongepowered.api.config.ConfigManager;
import org.spongepowered.api.data.DataManager;
import org.spongepowered.api.network.channel.ChannelManager;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.registry.BuilderProvider;
import org.spongepowered.api.registry.FactoryProvider;
import org.spongepowered.api.service.ServiceProvider;
import org.spongepowered.api.sql.SqlManager;
import org.spongepowered.api.util.metric.MetricsConfigManager;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeGame;
import org.spongepowered.common.SpongePlatform;
import org.spongepowered.common.asset.SpongeAssetManager;
import org.spongepowered.common.config.PluginConfigManager;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.network.channel.ChannelBufferAllocator;
import org.spongepowered.common.network.channel.SpongeChannelManager;
import org.spongepowered.common.registry.SpongeBuilderProvider;
import org.spongepowered.common.registry.SpongeFactoryProvider;
import org.spongepowered.common.service.game.SpongeGameScopedServiceProvider;
import org.spongepowered.common.sql.SpongeSqlManager;
import org.spongepowered.common.util.metric.SpongeMetricsConfigManager;

public final class SpongeCommonModule extends AbstractModule {

    @Override
    protected void configure() {
        this.bind(Game.class).to(SpongeGame.class);
        this.bind(MinecraftVersion.class).toInstance(SpongeCommon.minecraftVersion());
        this.bind(AssetManager.class).to(SpongeAssetManager.class);
        this.bind(ChannelManager.class).toInstance(new SpongeChannelManager(ChannelBufferAllocator.POOLED));
        this.bind(PluginManager.class).toInstance(Launch.instance().pluginManager());
        this.bind(DataManager.class).to(SpongeDataManager.class);
        this.bind(ConfigManager.class).to(PluginConfigManager.class);
        this.bind(MetricsConfigManager.class).to(SpongeMetricsConfigManager.class);
        this.bind(SqlManager.class).to(SpongeSqlManager.class);
        this.bind(ServiceProvider.GameScoped.class).to(SpongeGameScopedServiceProvider.class);
        this.bind(FactoryProvider.class).to(SpongeFactoryProvider.class);
        this.bind(BuilderProvider.class).to(SpongeBuilderProvider.class);

        this.requestStaticInjection(SpongeCommon.class);
        this.requestStaticInjection(Sponge.class);
    }
}
