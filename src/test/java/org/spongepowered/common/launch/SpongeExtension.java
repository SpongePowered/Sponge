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

import org.junit.jupiter.api.extension.TestInstantiationException;
import org.spongepowered.api.util.file.DeleteFileVisitor;
import org.spongepowered.common.applaunch.AppLaunch;
import org.spongepowered.common.launch.plugin.TestPluginPlatform;
import org.spongepowered.mij.ModLauncherExtension;
import org.spongepowered.mij.SharedModLauncher;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpongeExtension extends ModLauncherExtension {
    private static final String[] LAUNCHER_ARGS = resolveLauncherArguments();

    private static String[] resolveLauncherArguments() {
        List<String> args = new ArrayList<>();
        args.add("--launchTarget");
        args.add("sponge_client_test");
        args.add("--mixin.config");
        args.add("mixins.sponge.test.json");
        args.addAll(Arrays.asList(System.getProperty("sponge.test.launcherArguments", "").split(" ")));
        return args.toArray(new String[0]);
    }

    @Override
    protected ClassLoader getTransformingClassLoader() {
        if (AppLaunch.pluginPlatform() == null) {
            final TestPluginPlatform platform = new TestPluginPlatform();

            // Delete existing files to ensure consistency between runs
            if (Files.exists(platform.baseDirectory())) {
                try {
                    Files.walkFileTree(platform.baseDirectory(), DeleteFileVisitor.INSTANCE);
                } catch (final IOException e) {
                    throw new TestInstantiationException("Failed to delete directory " + platform.baseDirectory(), e);
                }
            }

            AppLaunch.setPluginPlatform(platform);
        }

        return SharedModLauncher.getTransformingClassLoader(LAUNCHER_ARGS);
    }
}
