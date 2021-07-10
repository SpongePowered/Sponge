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
import net.minecraft.server.Bootstrap;
import org.spongepowered.common.SpongeBootstrap;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeLifecycle;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.vanilla.applaunch.plugin.VanillaPluginPlatform;

public class IntegrationTestLaunch extends VanillaLaunch {
    private final boolean isServer;

    protected IntegrationTestLaunch(final VanillaPluginPlatform pluginPlatform, final boolean isServer) {
        super(pluginPlatform, Stage.DEVELOPMENT);
        this.isServer = isServer;
    }

    public static void launch(final VanillaPluginPlatform pluginPlatform, final Boolean isServer, final String[] args) {
        final IntegrationTestLaunch launcher = new IntegrationTestLaunch(pluginPlatform, isServer);
        Launch.setInstance(launcher);
        launcher.launchPlatform(args);
    }

    @Override
    public boolean dedicatedServer() {
        return this.isServer;
    }

    @Override
    protected void performBootstrap(String[] args) {
        this.logger().info("Running integration tests...");
        SpongeBootstrap.perform("integration tests", this::performIntegrationTests);
    }

    private void performIntegrationTests() {
        // Prepare Vanilla
        Bootstrap.bootStrap();
        Bootstrap.validate();

        // Prepare Sponge
        final SpongeLifecycle lifecycle = SpongeBootstrap.lifecycle();
        lifecycle.establishGlobalRegistries();
        lifecycle.establishDataProviders();
        lifecycle.callRegisterDataEvent();

        this.logger().info("Performing Mixin audit");
        Launch.instance().auditMixins();

        this.logger().info("Testing complete, goodbye!");
        SpongeCommon.game().asyncScheduler().close();
        System.exit(0);
    }
}
