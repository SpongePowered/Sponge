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
import org.spongepowered.api.Platform;
import org.spongepowered.api.Server;
import org.spongepowered.api.asset.AssetManager;
import org.spongepowered.api.command.manager.CommandManager;
import org.spongepowered.api.config.ConfigManager;
import org.spongepowered.api.data.DataManager;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.network.ChannelRegistrar;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.registry.GameRegistry;
import org.spongepowered.api.service.ServiceProvider;
import org.spongepowered.api.sql.SqlManager;
import org.spongepowered.api.util.metric.MetricsConfigManager;
import org.spongepowered.api.world.TeleportHelper;

public final class VanillaServerGame extends VanillaGame {

    private final Server server;

    @Inject
    public VanillaServerGame(final Server server, final Platform platform, final GameRegistry registry,
        final DataManager dataManager, final PluginManager pluginManager, final EventManager eventManager,
        final AssetManager assetManager, final ConfigManager configManager, final ChannelRegistrar channelRegistrar,
        final TeleportHelper teleportHelper, final CauseStackManager causeStackManager,
        final MetricsConfigManager metricsConfigManager, final CommandManager commandManager, final SqlManager sqlManager,
        final ServiceProvider serviceProvider) {

        super(platform, registry, dataManager, pluginManager, eventManager, assetManager, configManager, channelRegistrar, teleportHelper,
            causeStackManager, metricsConfigManager, commandManager, sqlManager, serviceProvider);
        this.server = server;
    }

    @Override
    public boolean isServerAvailable() {
        return true;
    }

    @Override
    public Server getServer() {
        return this.server;
    }
}
