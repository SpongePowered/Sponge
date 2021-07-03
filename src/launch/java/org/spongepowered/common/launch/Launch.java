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

import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import com.google.inject.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Platform;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.common.applaunch.plugin.PluginPlatform;
import org.spongepowered.common.launch.plugin.SpongePluginManager;
import org.spongepowered.plugin.PluginContainer;
import java.util.ArrayList;
import java.util.List;

public abstract class Launch {

    private static Launch INSTANCE;
    private static final String ID = "sponge";

    protected final PluginPlatform pluginPlatform;
    private final Logger logger;
    private final List<PluginContainer> launcherPlugins;
    private PluginContainer minecraftPlugin, apiPlugin, commonPlugin;

    protected Launch(final PluginPlatform pluginPlatform) {
        this.logger = LogManager.getLogger("launch");
        this.pluginPlatform = pluginPlatform;
        this.launcherPlugins = new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    public static <L extends Launch> L instance() {
        return (L) Launch.INSTANCE;
    }

    public static void setInstance(final Launch instance) {
        if (Launch.INSTANCE != null) {
            throw new RuntimeException("Attempt made to re-set launcher instance!");
        }

        Launch.INSTANCE = Preconditions.checkNotNull(instance);
    }

    public final String id() {
        return Launch.ID;
    }

    public abstract boolean dedicatedServer();

    public abstract SpongePluginManager pluginManager();

    public final Logger logger() {
        return this.logger;
    }

    public PluginPlatform pluginPlatform() {
        return this.pluginPlatform;
    }

    public abstract Stage injectionStage();

    public final boolean developerEnvironment() {
        return this.injectionStage() == Stage.DEVELOPMENT;
    }

    public final PluginContainer minecraftPlugin() {
        if (this.minecraftPlugin == null) {
            this.minecraftPlugin = this.pluginManager().plugin(PluginManager.MINECRAFT_PLUGIN_ID).orElse(null);

            if (this.minecraftPlugin == null) {
                throw new RuntimeException("Could not find the plugin representing Minecraft, this is a serious issue!");
            }
        }

        return this.minecraftPlugin;
    }

    public final PluginContainer apiPlugin() {
        if (this.apiPlugin == null) {
            this.apiPlugin = this.pluginManager().plugin(Platform.API_ID).orElse(null);

            if (this.apiPlugin == null) {
                throw new RuntimeException("Could not find the plugin representing SpongeAPI, this is a serious issue!");
            }
        }

        return this.apiPlugin;
    }

    public final PluginContainer commonPlugin() {
        if (this.commonPlugin == null) {
            this.commonPlugin = this.pluginManager().plugin(PluginManager.SPONGE_PLUGIN_ID).orElse(null);

            if (this.commonPlugin == null) {
                throw new RuntimeException("Could not find the plugin representing Sponge, this is a serious issue!");
            }
        }

        return this.commonPlugin;
    }

    public abstract PluginContainer platformPlugin();

    public final List<PluginContainer> launcherPlugins() {
        if (this.launcherPlugins.isEmpty()) {
            this.launcherPlugins.add(this.minecraftPlugin());
            this.launcherPlugins.add(this.apiPlugin());
            this.launcherPlugins.add(this.commonPlugin());
            this.launcherPlugins.add(this.platformPlugin());
        }

        return this.launcherPlugins;
    }

    public final void auditMixins() {
        MixinEnvironment.getCurrentEnvironment().audit();
    }

    public abstract Injector createInjector();

    public abstract void performLifecycle();
}
