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
package org.spongepowered.vanilla.launch;

import com.google.inject.Stage;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.Client;
import org.spongepowered.api.Server;
import org.spongepowered.common.SpongeBootstrap;
import org.spongepowered.common.launch.Launcher;
import org.spongepowered.vanilla.launch.plugin.PluginLoader;

import java.nio.file.Path;
import java.util.List;

public final class DedicatedServerLauncher extends VanillaLauncher {

    protected DedicatedServerLauncher(final PluginLoader pluginLoader, final Stage injectionStage) {
        super(pluginLoader, injectionStage);
    }

    public static void launch(final PluginLoader pluginLoader, final Boolean isDeveloperEnvironment, final String[] args) {
        final DedicatedServerLauncher launcher = new DedicatedServerLauncher(pluginLoader, isDeveloperEnvironment ? Stage.DEVELOPMENT : Stage.PRODUCTION);
        Launcher.setInstance(launcher);
        launcher.onLaunch(args);
    }

    @Override
    public boolean isDedicatedServer() {
        return true;
    }

    @Override
    protected void onLaunch(final String[] args) {
        super.onLaunch(args);
        this.getLogger().info("Loading Minecraft Server, please wait...");

        SpongeBootstrap.perform("Server", () -> MinecraftServer.main(args));
    }

}
