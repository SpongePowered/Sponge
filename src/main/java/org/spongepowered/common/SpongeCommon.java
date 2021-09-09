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

import net.minecraft.SharedConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.plugin.PluginContainer;

import java.io.IOException;
import java.nio.file.Path;

public final class SpongeCommon {

    private static final Logger LOGGER = LogManager.getLogger(Launch.instance().id());
    private static final SpongeMinecraftVersion MINECRAFT_VERSION = new SpongeMinecraftVersion(
        SharedConstants.getCurrentVersion().getName(),
        SharedConstants.getCurrentVersion().getProtocolVersion()
    );

    private static PluginContainer activePlugin = Launch.instance().minecraftPlugin();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                ((SpongeGame) Sponge.game()).configManager().close();
            } catch (final IOException e) {
                SpongeCommon.logger().error("Failed to shut down configuration watch service", e);
            } catch (final Exception ignored) {
            }
        }, "Sponge shutdown thread"));
    }

    private SpongeCommon() {
    }

    public static Logger logger() {
        return SpongeCommon.LOGGER;
    }

    public static Path gameDirectory() {
        return Launch.instance().pluginPlatform().baseDirectory();
    }

    public static PluginContainer activePlugin() {
        return SpongeCommon.activePlugin;
    }

    public static void setActivePlugin(final @Nullable PluginContainer plugin) {
        if (plugin == null) {
            SpongeCommon.activePlugin = Launch.instance().minecraftPlugin();
        } else {
            SpongeCommon.activePlugin = plugin;
        }
    }
}
