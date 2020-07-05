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
package org.spongepowered.vanilla.modlauncher.bootstrap;

import cpw.mods.gross.Java9ClassLoaderUtil;
import cpw.mods.modlauncher.api.ILaunchHandlerService;
import cpw.mods.modlauncher.api.ITransformingClassLoader;
import cpw.mods.modlauncher.api.ITransformingClassLoaderBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.vanilla.modlauncher.Main;
import org.spongepowered.plugin.PluginEnvironment;
import org.spongepowered.plugin.PluginKeys;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;

public final class ServerDevLaunchHandler implements ILaunchHandlerService {

    private final Logger logger;

    public ServerDevLaunchHandler() {
        this.logger = LogManager.getLogger("Sponge");
    }

    @Override
    public String name() {
        return "sponge_server_dev";
    }

    @Override
    public void configureTransformationClassLoader(final ITransformingClassLoaderBuilder builder) {
        // TODO Minecraft 1.14 - Very much not correct...
        for (final URL url : Java9ClassLoaderUtil.getSystemClassPathURLs()) {
            if (url.toString().contains("mixin") && url.toString().endsWith(".jar")) {
                continue;
            }

            try {
                builder.addTransformationPath(Paths.get(url.toURI()));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Callable<Void> launchService(final String[] arguments, final ITransformingClassLoader launchClassLoader) {

        this.logger.info("Transitioning to Sponge launcher, please wait...");

        return () -> {
            final PluginEnvironment launchPluginEnvironment = Main.getLaunchPluginEnvironment();
            Class.forName("org.spongepowered.vanilla.launch.ServerLauncher", true, launchClassLoader.getInstance()).getMethod("launch", String.class,
                Path.class, List.class, String[].class).invoke(null,
                launchPluginEnvironment.getBlackboard().get(PluginKeys.VERSION).orElse(null),
                launchPluginEnvironment.getBlackboard().get(PluginKeys.BASE_DIRECTORY).orElse(null),
                launchPluginEnvironment.getBlackboard().get(PluginKeys.PLUGIN_DIRECTORIES).orElse(null),
                arguments);
            return null;
        };
    }
}
