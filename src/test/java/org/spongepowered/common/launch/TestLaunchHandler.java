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
package org.spongepowered.common.launch;

import cpw.mods.modlauncher.api.ITransformingClassLoader;
import cpw.mods.modlauncher.api.ITransformingClassLoaderBuilder;
import org.spongepowered.common.applaunch.AppLaunch;
import org.spongepowered.common.launch.plugin.TestPluginPlatform;
import org.spongepowered.vanilla.applaunch.handler.VanillaBaseLaunchHandler;

import java.util.Optional;
import java.util.concurrent.Callable;

public class TestLaunchHandler extends VanillaBaseLaunchHandler {

    @Override
    protected boolean isDev() {
        return true;
    }

    @Override
    public String name() {
        return "sponge_client_test";
    }

    @Override
    public void configureTransformationClassLoader(final ITransformingClassLoaderBuilder builder) {
        super.configureTransformationClassLoader(builder);
        builder.setManifestLocator(connection -> Optional.empty());
    }

    @Override
    public Callable<Void> launchService(String[] arguments, ITransformingClassLoader launchClassLoader) {
        launchClassLoader.addTargetPackageFilter(s -> !s.startsWith("org.mockito.") && !s.startsWith("org.junit."));
        return super.launchService(arguments, launchClassLoader);
    }

    @Override
    protected void launchService0(String[] arguments, ITransformingClassLoader launchClassLoader) throws Exception {
        if (AppLaunch.pluginPlatform() == null) {
            final TestPluginPlatform platform = new TestPluginPlatform();
            AppLaunch.setPluginPlatform(platform);
        }

        Class.forName("org.spongepowered.common.launch.TestLaunch", true, launchClassLoader.getInstance()).getMethod("launch").invoke(null);
    }
}
