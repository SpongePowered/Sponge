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

import cpw.mods.modlauncher.api.ITransformingClassLoader;
import org.spongepowered.plugin.PluginEnvironment;
import org.spongepowered.plugin.PluginKeys;
import org.spongepowered.vanilla.modlauncher.Main;

import java.nio.file.Path;
import java.util.List;

public final class ServerDevLaunchHandler extends AbstractSpongeDevLaunchHandler {

    @Override
    public String name() {
        return "sponge_server_dev";
    }

    @Override
    protected void launchService0(final String[] arguments, final ITransformingClassLoader launchClassLoader) throws Exception {
        final PluginEnvironment launchPluginEnvironment = Main.getLaunchPluginEnvironment();
        Class.forName("org.spongepowered.vanilla.launch.DedicatedServerLauncher", true, launchClassLoader.getInstance()).getMethod("launch", String.class,
                Path.class, List.class, Boolean.class, String[].class).invoke(null,
                launchPluginEnvironment.getBlackboard().get(PluginKeys.VERSION).orElse(null),
                launchPluginEnvironment.getBlackboard().get(PluginKeys.BASE_DIRECTORY).orElse(null),
                launchPluginEnvironment.getBlackboard().get(PluginKeys.PLUGIN_DIRECTORIES).orElse(null),
                Boolean.TRUE,
                arguments);
    }

}
