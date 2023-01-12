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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import net.minecraft.server.Bootstrap;
import org.spongepowered.common.SpongeLifecycle;
import org.spongepowered.common.applaunch.AppLaunch;
import org.spongepowered.common.applaunch.plugin.PluginPlatform;
import org.spongepowered.common.inject.SpongeCommonModule;
import org.spongepowered.common.launch.inject.TestModule;
import org.spongepowered.common.launch.plugin.VanillaBasePluginManager;
import org.spongepowered.plugin.PluginContainer;

public class TestLaunch extends VanillaBaseLaunch {

    protected TestLaunch(final PluginPlatform pluginPlatform) {
        super(pluginPlatform, new VanillaBasePluginManager());
    }

    @Override
    public boolean dedicatedServer() {
        return false;
    }

    @Override
    public Stage injectionStage() {
        return Stage.DEVELOPMENT;
    }

    @Override
    public PluginContainer platformPlugin() {
        return null;
    }

    @Override
    public Injector createInjector() {
        return Guice.createInjector(this.injectionStage(), new SpongeCommonModule(), new TestModule());
    }

    private void startLifecycle() {
        final SpongeLifecycle lifecycle = this.createInjector().getInstance(SpongeLifecycle.class);
        this.setLifecycle(lifecycle);
        lifecycle.establishFactories();
        lifecycle.establishBuilders();
        lifecycle.establishGameServices();
        lifecycle.establishDataKeyListeners();

        Bootstrap.bootStrap();
        Bootstrap.validate();

        lifecycle.establishGlobalRegistries();
        lifecycle.establishDataProviders();
    }

    public static void launch() {
        final TestLaunch launch = new TestLaunch(AppLaunch.pluginPlatform());
        Launch.setInstance(launch);
        launch.launchPlatform(new String[0]);
    }

    @Override
    protected void performBootstrap(String[] args) {
        this.startLifecycle();
    }
}
