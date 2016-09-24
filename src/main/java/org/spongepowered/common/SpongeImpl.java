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

import static com.google.common.base.Objects.firstNonNull;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.spongepowered.common.config.SpongeConfig.Type.GLOBAL;

import com.google.inject.Injector;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameState;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.SpongeEventFactoryUtils;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.game.state.GameStateEvent;
import org.spongepowered.api.event.game.state.GameStoppedEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.type.GlobalConfig;
import org.spongepowered.common.event.SpongeEventManager;
import org.spongepowered.common.launch.SpongeLaunch;
import org.spongepowered.common.registry.SpongeGameRegistry;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public final class SpongeImpl {

    public static final String GAME_ID = "minecraft";
    public static final String GAME_NAME = "Minecraft";

    public static final String API_NAME = firstNonNull(getPackage().getSpecificationTitle(), "SpongeAPI");
    public static final Optional<String> API_VERSION = Optional.ofNullable(getPackage().getSpecificationVersion());

    public static final String ECOSYSTEM_ID = "sponge";
    public static final String ECOSYSTEM_NAME = "Sponge";

    public static final Optional<String> IMPLEMENTATION_NAME = Optional.ofNullable(getPackage().getImplementationTitle());
    public static final Optional<String> IMPLEMENTATION_VERSION =  Optional.ofNullable(getPackage().getImplementationVersion());

    // TODO: Keep up to date
    public static final SpongeMinecraftVersion MINECRAFT_VERSION = new SpongeMinecraftVersion("1.10.2", 210);

    private static final Logger logger = LogManager.getLogger(ECOSYSTEM_NAME);
    private static final org.slf4j.Logger slf4jLogger = LoggerFactory.getLogger(ECOSYSTEM_NAME);

    @Nullable
    private static SpongeImpl instance;

    @Nullable private static SpongeConfig<GlobalConfig> globalConfig;


    public static final Random random = new Random();

    private final Injector injector;
    private final Game game;
    private final PluginContainer plugin;
    private final PluginContainer minecraftPlugin;
    private final Cause implementationCause;

    @Nullable
    private static List<PluginContainer> components;

    @Inject
    public SpongeImpl(Injector injector, Game game, @Named(ECOSYSTEM_ID) PluginContainer plugin, @Named(GAME_ID) PluginContainer minecraftPlugin) {
        checkState(instance == null, "SpongeImpl was already initialized");
        instance = this;

        this.injector = checkNotNull(injector, "injector");
        this.game = checkNotNull(game, "game");
        this.plugin = checkNotNull(plugin, "plugin");
        this.minecraftPlugin = checkNotNull(minecraftPlugin, "minecraftPlugin");
        this.implementationCause = Cause.source(this.plugin).build();
    }

    public static SpongeImpl getInstance() {
        checkState(instance != null, "SpongeImpl was not initialized");
        return instance;
    }

    public static boolean isInitialized() {
        return instance != null;
    }

    public static Injector getInjector() {
        return getInstance().injector;
    }

    public static SpongeGame getGame() {
        return (SpongeGame) getInstance().game;
    }

    public static MinecraftServer getServer() {
        return (MinecraftServer) getGame().getServer();
    }

    public static SpongeGameRegistry getRegistry() {
        return getGame().getRegistry();
    }

    public static boolean postEvent(Event event) {
        return getGame().getEventManager().post(event);
    }

    public static Logger getLogger() {
        return logger;
    }

    public static org.slf4j.Logger getSlf4jLogger() {
        return slf4jLogger;
    }

    public static PluginContainer getPlugin() {
        return getInstance().plugin;
    }

    public static PluginContainer getMinecraftPlugin() {
        return getInstance().minecraftPlugin;
    }

    public static Path getGameDir() {
        return SpongeLaunch.getGameDir();
    }

    public static Path getConfigDir() {
        return SpongeLaunch.getConfigDir();
    }

    public static Path getPluginsDir() {
        return SpongeLaunch.getPluginsDir();
    }

    public static Path getSpongeConfigDir() {
        return SpongeLaunch.getSpongeConfigDir();
    }

    public static SpongeConfig<GlobalConfig> getGlobalConfig() {
        if (globalConfig == null) {
            globalConfig = new SpongeConfig<>(GLOBAL, getSpongeConfigDir().resolve("global.conf"), ECOSYSTEM_ID);
        }

        return globalConfig;
    }

    public static List<PluginContainer> getInternalPlugins() {
        if (components == null) {
            components = new ArrayList<>(6);
            components.add(SpongeImpl.getMinecraftPlugin());
            components.add(Sponge.getPlatform().getApi());
            components.add(Sponge.getPlatform().getImplementation());
        }

        return components;
    }

    public static void postState(Class<? extends GameStateEvent> type, GameState state) {
        getGame().setState(state);
        ((SpongeEventManager) getGame().getEventManager()).post(SpongeEventFactoryUtils.createState(type, getGame()), true);
    }

    public static void postShutdownEvents() {
        postState(GameStoppingEvent.class, GameState.GAME_STOPPING);
        postState(GameStoppedEvent.class, GameState.GAME_STOPPED);
    }

    private static Package getPackage() {
        return SpongeImpl.class.getPackage();
    }

    public static Cause getImplementationCause() {
        return getInstance().implementationCause;
    }
}
