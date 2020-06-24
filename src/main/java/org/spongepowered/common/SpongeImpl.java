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

import static com.google.common.base.Preconditions.checkState;
import static org.spongepowered.api.Platform.Component.IMPLEMENTATION;
import static org.spongepowered.common.config.SpongeConfig.Type.CUSTOM_DATA;
import static org.spongepowered.common.config.SpongeConfig.Type.GLOBAL;
import static org.spongepowered.common.config.SpongeConfig.Type.TRACKER;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.GameState;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.game.state.GameStateEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.command.SpongeCommandManager;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.SpongeConfigSaveManager;
import org.spongepowered.common.config.type.CustomDataConfig;
import org.spongepowered.common.config.type.GlobalConfig;
import org.spongepowered.common.config.type.TrackerConfig;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.property.SpongePropertyRegistry;
import org.spongepowered.common.event.SpongeCauseStackManager;
import org.spongepowered.common.event.SpongeEventManager;
import org.spongepowered.common.launch.SpongeLaunch;
import org.spongepowered.common.registry.SpongeGameRegistry;
import org.spongepowered.common.scheduler.SpongeScheduler;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nullable;

@Singleton
public final class SpongeImpl {

    public static final String GAME_ID = "minecraft";
    public static final String GAME_NAME = "Minecraft";

    public static final String API_NAME = "SpongeAPI";

    public static final String ECOSYSTEM_ID = "sponge"; // This is different from the id used by the actual implementation
    public static final String ECOSYSTEM_NAME = "Sponge";

    // TODO: Keep up to date
    public static final SpongeMinecraftVersion MINECRAFT_VERSION = new SpongeMinecraftVersion("1.12.2", 340);

    private static final Logger logger = LogManager.getLogger(ECOSYSTEM_NAME);
    public static final Random random = new Random();

    // Can't @Inject these because they are referenced before everything is initialized
    @Nullable private static SpongeConfig<GlobalConfig> globalConfigAdapter;
    @Nullable private static SpongeConfig<TrackerConfig> trackerConfigAdapter;
    @Nullable private static SpongeConfig<CustomDataConfig> customDataConfigAdapter;
    @Nullable private static SpongeConfigSaveManager configSaveManager;
    @Nullable private static PluginContainer minecraftPlugin;
    @Nullable private static PluginContainer spongecommon;

    @Inject @Nullable private static SpongeGame game;
    @Inject @Nullable private static SpongeGameRegistry registry;
    @Inject @Nullable private static SpongeDataManager dataManager;
    @Inject @Nullable private static SpongePropertyRegistry propertyRegistry;
    @Inject @Nullable private static SpongeScheduler scheduler;
    @Inject @Nullable private static SpongeCommandManager commandManager;
    @Inject @Nullable private static SpongeCauseStackManager causeStackManager;

    private static final List<PluginContainer> internalPlugins = new ArrayList<>();

    private SpongeImpl() {
    }

    @Inject
    private static void initialize(Platform platform) {
        if (minecraftPlugin == null) {
            minecraftPlugin = platform.getContainer(Platform.Component.GAME);
        }


        for (Platform.Component component : Platform.Component.values()) {
            internalPlugins.add(platform.getContainer(component));
            if (component == Platform.Component.API && platform instanceof SpongePlatform) {
                // We want to set up the common version after the api.
                internalPlugins.add(((SpongePlatform) platform).getCommon());
            }
        }
    }

    private static <T> T check(@Nullable T instance) {
        checkState(instance != null, "SpongeImpl has not been initialized!");
        return instance;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static boolean isInitialized() {
        return game != null;
    }

    public static SpongeGame getGame() {
        return check(game);
    }

    public static MinecraftServer getServer() {
        return (MinecraftServer) Sponge.getServer();
    }

    public static SpongeGameRegistry getRegistry() {
        return check(registry);
    }

    public static SpongeDataManager getDataManager() {
        return check(dataManager);
    }

    public static SpongePropertyRegistry getPropertyRegistry() {
        return check(propertyRegistry);
    }

    public static SpongeScheduler getScheduler() {
        return check(scheduler);
    }

    public static SpongeCommandManager getCommandManager() {
        return check(commandManager);
    }

    public static SpongeCauseStackManager getCauseStackManager() {
        return check(causeStackManager);
    }

    public static PluginContainer getPlugin() {
        return Sponge.getPlatform().getContainer(IMPLEMENTATION);
    }

    public static PluginContainer getMinecraftPlugin() {
        checkState(minecraftPlugin != null, "Minecraft plugin container is not initialized");
        return minecraftPlugin;
    }

    public static void setMinecraftPlugin(PluginContainer minecraft) {
        checkState(minecraftPlugin == null, "Minecraft plugin container is already initialized");
        minecraftPlugin = minecraft;
    }

    public static void setSpongePlugin(PluginContainer common) {
        checkState(spongecommon == null);
        spongecommon = common;
    }

    public static PluginContainer getSpongePlugin() {
        checkState(spongecommon != null, "SpongeCommon plugin container is not initialized");
        return spongecommon;
    }

    public static Path getGameDir() {
        return SpongeLaunch.getGameDir();
    }

    public static Path getPluginConfigDir() {
        return SpongeLaunch.getPluginConfigDir();
    }

    public static Path getPluginsDir() {
        return SpongeLaunch.getPluginsDir();
    }

    public static Path getSpongeConfigDir() {
        return SpongeLaunch.getSpongeConfigDir();
    }

    public static SpongeConfigSaveManager getConfigSaveManager() {
        if (configSaveManager == null) {
            configSaveManager = new SpongeConfigSaveManager();
        }

        return configSaveManager;
    }

    public static SpongeConfig<GlobalConfig> getGlobalConfigAdapter() {
        if (globalConfigAdapter == null) {
            globalConfigAdapter = new SpongeConfig<>(GLOBAL, getSpongeConfigDir().resolve("global.conf"), ECOSYSTEM_ID, null, false);
        }

        return globalConfigAdapter;
    }

    public static SpongeConfig<CustomDataConfig> getCustomDataConfigAdapter() {
        if (customDataConfigAdapter == null) {
            customDataConfigAdapter = new SpongeConfig<>(CUSTOM_DATA, getSpongeConfigDir().resolve("custom_data.conf"), ECOSYSTEM_ID, null, true);
        }
        return customDataConfigAdapter;
    }

    public static SpongeConfig<TrackerConfig> getTrackerConfigAdapter() {
        if (trackerConfigAdapter == null) {
            trackerConfigAdapter = new SpongeConfig<>(TRACKER, getSpongeConfigDir().resolve("tracker.conf"), ECOSYSTEM_ID, null, true);
        }
        return trackerConfigAdapter;
    }

    public static List<PluginContainer> getInternalPlugins() {
        return internalPlugins;
    }

    /**
     * Throws the given event.
     *
     * @param event The event
     * @return True if the event is cancellable and is cancelled, false if not cancelled
     */
    public static boolean postEvent(Event event) {
        return Sponge.getEventManager().post(event);
    }

    public static boolean postEvent(Event event, boolean allowClient) {
        // TODO quick and dirty fix (cant cast in UnitTest)
        if (Sponge.getEventManager() instanceof SpongeEventManager) {
            return ((SpongeEventManager) Sponge.getEventManager()).post(event, allowClient);
        }
        return true;
    }

    public static void postState(GameState state, GameStateEvent event) {
        check(game);
        game.setState(state);
        postEvent(event, true);
    }

    public static void postShutdownEvents() {
        check(game);
        postState(GameState.GAME_STOPPING, SpongeEventFactory.createGameStoppingEvent(Sponge.getCauseStackManager().getCurrentCause()));
        postState(GameState.GAME_STOPPED, SpongeEventFactory.createGameStoppedEvent(Sponge.getCauseStackManager().getCurrentCause()));
    }

    // TODO this code is used a BUNCH of times
    /**
     * Gets the {@link PluginContainer} for given plugin object.
     *
     * @param plugin The Plugin Object
     * @return The associated plugin container
     * @throws IllegalArgumentException when the argument has no associated plugin container (usually because it is not a plugin)
     */
    public static PluginContainer getPluginContainer(Object plugin) throws IllegalArgumentException {
        Optional<PluginContainer> containerOptional = Sponge.getGame().getPluginManager().fromInstance(plugin);
        if (!containerOptional.isPresent()) {
            throw new IllegalArgumentException(
                    "The provided plugin object does not have an associated plugin container "
                            + "(in other words, is 'plugin' actually your plugin object?");
        }

        return containerOptional.get();
    }

    public static int directionToIndex(Direction direction) {
        switch (direction) {
            case NORTH:
            case NORTHEAST:
            case NORTHWEST:
                return 0;
            case SOUTH:
            case SOUTHEAST:
            case SOUTHWEST:
                return 1;
            case EAST:
                return 2;
            case WEST:
                return 3;
            default:
                throw new IllegalArgumentException("Unexpected direction");
        }
    }

    public static Direction getCardinalDirection(Direction direction) {
        switch (direction) {
            case NORTH:
            case NORTHEAST:
            case NORTHWEST:
                return Direction.NORTH;
            case SOUTH:
            case SOUTHEAST:
            case SOUTHWEST:
                return Direction.SOUTH;
            case EAST:
                return Direction.EAST;
            case WEST:
                return Direction.WEST;
            default:
                throw new IllegalArgumentException("Unexpected direction");
        }
    }

    public static Direction getSecondaryDirection(Direction direction) {
        switch (direction) {
            case NORTHEAST:
            case SOUTHEAST:
                return Direction.EAST;
            case NORTHWEST:
            case SOUTHWEST:
                return Direction.WEST;
            default:
                return Direction.NONE;
        }
    }
}
