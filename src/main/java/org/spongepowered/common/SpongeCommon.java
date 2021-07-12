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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.minecraft.SharedConstants;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Event;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.scheduler.AsyncScheduler;
import org.spongepowered.common.scheduler.ServerScheduler;
import org.spongepowered.plugin.PluginContainer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Singleton
public final class SpongeCommon {

    private static final Logger LOGGER = LogManager.getLogger(Launch.instance().id());
    private static final SpongeMinecraftVersion MINECRAFT_VERSION = new SpongeMinecraftVersion(
        SharedConstants.getCurrentVersion().getName(),
        SharedConstants.getCurrentVersion().getProtocolVersion()
    );

    @Inject private @Nullable static SpongeGame game;
    private @Nullable static PluginContainer activePlugin;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            final @Nullable SpongeGame game = SpongeCommon.game;
            if (game != null) {
                try {
                    game.configManager().close();
                } catch (final IOException e) {
                    SpongeCommon.logger().error("Failed to shut down configuration watch service", e);
                }
            }
        }, "Sponge shutdown thread"));
    }

    private SpongeCommon() {
    }

    public static boolean initialized() {
        return SpongeCommon.game != null;
    }

    public static Logger logger() {
        return SpongeCommon.LOGGER;
    }

    public static SpongeMinecraftVersion minecraftVersion() {
        return SpongeCommon.MINECRAFT_VERSION;
    }

    public static SpongeGame game() {
        if (SpongeCommon.game == null) {
            throw new IllegalStateException("SpongeCommon has not been initialized yet!");
        }

        return SpongeCommon.game;
    }

    public static MinecraftServer server() {
        return (MinecraftServer) Sponge.server();
    }

    public static ServerScheduler serverScheduler() {
        return (ServerScheduler) Sponge.server().scheduler();
    }

    public static AsyncScheduler asyncScheduler() {
        return SpongeCommon.game().asyncScheduler();
    }

    public static Path gameDirectory() {
        return Launch.instance().pluginPlatform().baseDirectory();
    }

    public static Path pluginConfigDirectory() {
        return Paths.get(SpongeConfigs.getCommon().get().general.configDir.getParsed());
    }

    public static Path spongeConfigDirectory() {
        return SpongeCommon.gameDirectory().resolve("config");
    }

    public static PluginContainer activePlugin() {
        if (SpongeCommon.activePlugin == null) {
            return Launch.instance().minecraftPlugin();
        }

        return SpongeCommon.activePlugin;
    }

    public static void setActivePlugin(final @Nullable PluginContainer plugin) {
        SpongeCommon.activePlugin = plugin;
    }

    public static boolean post(final Event event) {
        return Sponge.eventManager().post(event);
    }
}
