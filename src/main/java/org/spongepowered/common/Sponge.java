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
package org.spongepowered.common;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.spongepowered.common.configuration.SpongeConfig.Type.GLOBAL;

import com.google.inject.Injector;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.slf4j.impl.SLF4JLogger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Platform;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.ProviderExistsException;
import org.spongepowered.api.service.command.CommandService;
import org.spongepowered.api.service.command.SimpleCommandService;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.service.persistence.SerializationService;
import org.spongepowered.api.service.rcon.RconService;
import org.spongepowered.api.service.scheduler.AsynchronousScheduler;
import org.spongepowered.api.service.scheduler.SynchronousScheduler;
import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.common.command.SpongeCommandDisambiguator;
import org.spongepowered.common.configuration.SpongeConfig;
import org.spongepowered.common.launch.SpongeLaunch;
import org.spongepowered.common.registry.SpongeGameRegistry;
import org.spongepowered.common.service.pagination.SpongePaginationService;
import org.spongepowered.common.service.persistence.SpongeSerializationService;
import org.spongepowered.common.service.rcon.MinecraftRconService;
import org.spongepowered.common.service.scheduler.AsyncScheduler;
import org.spongepowered.common.service.scheduler.SyncScheduler;
import org.spongepowered.common.service.sql.SqlServiceImpl;

import java.io.File;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class Sponge {

    @Nullable
    private static Sponge instance;

    public static Sponge getInstance() {
        checkState(instance != null, "Sponge was not initialized");
        return instance;
    }

    private final Injector injector;
    private final Game game;
    private final Logger logger;
    private final org.slf4j.Logger slf4jLogger;

    private final PluginContainer plugin;
    private final PluginContainer minecraftPlugin;

    @Inject
    public Sponge(Injector injector, Game game, Logger logger,
            @Named("Sponge") PluginContainer plugin, @Named("Minecraft") PluginContainer minecraftPlugin) {

        checkState(instance == null, "Sponge was already initialized");
        instance = this;

        this.injector = checkNotNull(injector, "injector");
        this.game = checkNotNull(game, "game");
        this.logger = checkNotNull(logger, "logger");
        this.slf4jLogger = new SLF4JLogger((AbstractLogger) this.logger, this.logger.getName());

        this.plugin = checkNotNull(plugin, "plugin");
        this.minecraftPlugin = checkNotNull(minecraftPlugin, "minecraftPlugin");
    }

    public void registerServices() {
        try {
            SimpleCommandService commandService = new SimpleCommandService(this.game, this.slf4jLogger, new SpongeCommandDisambiguator(this.game));
            this.game.getServiceManager().setProvider(this.plugin, CommandService.class, commandService);
        } catch (ProviderExistsException e) {
            this.logger.warn("Non-Sponge CommandService already registered: " + e.getLocalizedMessage());
        }

        try {
            this.game.getServiceManager().setProvider(this.plugin, SqlService.class, new SqlServiceImpl());
        } catch (ProviderExistsException e) {
            this.logger.warn("Non-Sponge SqlService already registered: " + e.getLocalizedMessage());
        }

        try {
            this.game.getServiceManager().setProvider(this.plugin, SynchronousScheduler.class, SyncScheduler.getInstance());
            this.game.getServiceManager().setProvider(this.plugin, AsynchronousScheduler.class, AsyncScheduler.getInstance());
        } catch (ProviderExistsException e) {
            this.logger.error("Non-Sponge scheduler has been registered. Cannot continue!");
            throw new ExceptionInInitializerError(e);
        }

        try {
            SerializationService serializationService = new SpongeSerializationService();
            this.game.getServiceManager().setProvider(this.plugin, SerializationService.class, serializationService);
        } catch (ProviderExistsException e2) {
            this.logger.warn("Non-Sponge SerializationService already registered: " + e2.getLocalizedMessage());
        }

        try {
            this.game.getServiceManager().setProvider(this.plugin, PaginationService.class, new SpongePaginationService());
        } catch (ProviderExistsException e) {
            this.logger.warn("Non-Sponge PaginationService already registered: " + e.getLocalizedMessage());
        }

        if (this.game.getPlatform().getType() == Platform.Type.SERVER) {
            try {
                this.game.getServiceManager().setProvider(this.plugin, RconService.class, new MinecraftRconService((DedicatedServer)
                        MinecraftServer.getServer()));
            } catch (ProviderExistsException e) {
                this.logger.warn("Non-Sponge Rcon service already registered: " + e.getLocalizedMessage());
            }
        }

    }

    public static Injector getInjector() {
        return getInstance().injector;
    }

    public static Game getGame() {
        return getInstance().game;
    }

    public static SpongeGameRegistry getSpongeRegistry() {
        return ((SpongeGameRegistry) getInstance().game.getRegistry());
    }

    public static Logger getLogger() {
        return getInstance().logger;
    }

    public static PluginContainer getPlugin() {
        return getInstance().plugin;
    }

    public static PluginContainer getMinecraftPlugin() {
        return getInstance().minecraftPlugin;
    }

    private static final File gameDir = SpongeLaunch.getGameDirectory();
    private static final File configDir = SpongeLaunch.getConfigDirectory();
    private static final File pluginsDir = SpongeLaunch.getPluginsDirectory();

    @Nullable private static SpongeConfig<SpongeConfig.GlobalConfig> globalConfig;

    public static File getGameDirectory() {
        return gameDir;
    }

    public static File getConfigDirectory() {
        return configDir;
    }

    public static File getPluginsDirectory() {
        return pluginsDir;
    }

    public static SpongeConfig<SpongeConfig.GlobalConfig> getGlobalConfig() {
        if (globalConfig == null) {
            globalConfig = new SpongeConfig<SpongeConfig.GlobalConfig>(GLOBAL, new File(new File(configDir, "sponge"), "global.conf"), "sponge");
        }

        return globalConfig;
    }

}
