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
import net.minecraft.gametest.framework.GameTestMainUtil;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.vanilla.applaunch.plugin.VanillaPluginPlatform;

/**
 * Runs Minecraft's headless {@code GameTestServer} with Sponge loaded. Tests and
 * their registration come from the {@code gametest} source sets on the classpath.
 */
public final class GameTestLaunch extends VanillaLaunch {

    private GameTestLaunch(final VanillaPluginPlatform pluginPlatform) {
        super(pluginPlatform, Stage.DEVELOPMENT);
    }

    public static void launch(final VanillaPluginPlatform pluginPlatform, final String[] args) {
        final GameTestLaunch launcher = new GameTestLaunch(pluginPlatform);
        Launch.setInstance(launcher);
        launcher.launchPlatform(args);
    }

    @Override
    public boolean dedicatedServer() {
        return true;
    }

    @Override
    protected void performBootstrap(final String[] args) {
        VanillaBootstrap.perform("GameTest Server", () -> {
            try {
                GameTestMainUtil.runGameTestServer(args, universe -> {});
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
