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

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.applaunch.config.core.InheritableConfigHandle;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;
import org.spongepowered.common.applaunch.config.inheritable.GlobalConfig;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.registry.SpongeGameRegistry;
import org.spongepowered.common.scheduler.AsyncScheduler;
import org.spongepowered.common.scheduler.ServerScheduler;
import org.spongepowered.common.util.Constants;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.PluginKeys;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Singleton
public final class SpongeCommon {

    public static final String ECOSYSTEM_ID = "sponge";

    public static final SpongeMinecraftVersion MINECRAFT_VERSION = new SpongeMinecraftVersion(
            Constants.MINECRAFT_VERSION,
            Constants.MINECRAFT_PROTOCOL_VERSION
    );

    // Can't @Inject these because they are referenced before everything is initialized
    @Nullable private static InheritableConfigHandle<GlobalConfig> globalConfigAdapter;

    @Inject @Nullable private static SpongeGame game;

    @Nullable private static PluginContainer activePlugin;

    private SpongeCommon() {
    }

    private static <T> T check(@Nullable T instance) {
        Preconditions.checkState(instance != null, "SpongeCommon has not been initialized!");
        return instance;
    }

    public static Logger getLogger() {
        return Launch.getInstance().getLogger();
    }

    public static boolean isInitialized() {
        return SpongeCommon.game != null;
    }

    public static SpongeGame getGame() {
        return SpongeCommon.check(SpongeCommon.game);
    }

    public static MinecraftServer getServer() {
        return (MinecraftServer) Sponge.getServer();
    }

    public static SpongeGameRegistry getRegistry() {
        return (SpongeGameRegistry) Sponge.getRegistry();
    }

    public static ServerScheduler getServerScheduler() {
        return (ServerScheduler) Sponge.getServer().getScheduler();
    }

    public static AsyncScheduler getAsyncScheduler() {
        return getGame().getAsyncScheduler();
    }

    public static Path getGameDirectory() {
        return Launch.getInstance().getPluginEngine().getPluginEnvironment().getBlackboard().get(PluginKeys.BASE_DIRECTORY)
                .orElseThrow(() -> new IllegalStateException("No game directory has been set in the launcher!"));
    }

    public static Path getPluginConfigDirectory() {
        return Paths.get(SpongeConfigs.getCommon().get().getGeneral().configDir());
    }

    public static Path getSpongeConfigDirectory() {
        return SpongeCommon.getGameDirectory().resolve("config");
    }

    @Deprecated
    public static PluginContainer getMinecraftPlugin() {
        return Launch.getInstance().getMinecraftPlugin();
    }

    @Deprecated
    public static PluginContainer getPlugin() {
        return Launch.getInstance().getCommonPlugin();
    }

    @Deprecated
    public static List<PluginContainer> getInternalPlugins() {
        return Launch.getInstance().getLauncherPlugins();
    }

    public static PluginContainer getActivePlugin() {
        if (SpongeCommon.activePlugin == null) {
            return Launch.getInstance().getMinecraftPlugin();
        }

        return SpongeCommon.activePlugin;
    }

    public static void setActivePlugin(@Nullable final PluginContainer plugin) {
        SpongeCommon.activePlugin = plugin;
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

    @Deprecated
    public static boolean postEvent(Event event, boolean allowClientThread) {
        return Sponge.getEventManager().post(event);
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
