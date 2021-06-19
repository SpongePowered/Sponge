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
import com.google.inject.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Platform;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.common.applaunch.plugin.PluginEngine;
import org.spongepowered.common.launch.plugin.SpongePluginManager;
import org.spongepowered.plugin.PluginContainer;
import java.util.ArrayList;
import java.util.List;

public abstract class Launch {

    private static Launch INSTANCE;

    protected final PluginEngine pluginEngine;
    protected final SpongePluginManager pluginManager;
    private final Logger logger;
    private final List<PluginContainer> launcherPlugins;
    private PluginContainer minecraftPlugin, apiPlugin, commonPlugin;

    protected Launch(final PluginEngine pluginEngine, final SpongePluginManager pluginManager) {
        this.logger = LogManager.getLogger("Sponge");
        this.pluginEngine = pluginEngine;
        this.pluginManager = pluginManager;
        this.launcherPlugins = new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    public static <L extends Launch> L getInstance() {
        return (L) Launch.INSTANCE;
    }

    public static void setInstance(Launch instance) {
        if (Launch.INSTANCE != null) {
            throw new RuntimeException("Attempt made to re-set launcher instance!");
        }

        Launch.INSTANCE = Preconditions.checkNotNull(instance);
    }

    public abstract boolean isVanilla();

    public abstract boolean isDedicatedServer();

    public final Logger getLogger() {
        return this.logger;
    }

    public PluginEngine getPluginEngine() {
        return this.pluginEngine;
    }

    public SpongePluginManager getPluginManager() {
        return this.pluginManager;
    }

    public abstract Stage getInjectionStage();

    public final boolean isDeveloperEnvironment() {
        return this.getInjectionStage() == Stage.DEVELOPMENT;
    }

    public final PluginContainer getMinecraftPlugin() {
        if (this.minecraftPlugin == null) {
            this.minecraftPlugin = this.pluginManager.plugin(PluginManager.MINECRAFT_PLUGIN_ID).orElse(null);

            if (this.minecraftPlugin == null) {
                throw new RuntimeException("Could not find the plugin representing Minecraft, this is a serious issue!");
            }
        }

        return this.minecraftPlugin;
    }

    public final PluginContainer getApiPlugin() {
        if (this.apiPlugin == null) {
            this.apiPlugin = this.pluginManager.plugin(Platform.API_ID).orElse(null);

            if (this.apiPlugin == null) {
                throw new RuntimeException("Could not find the plugin representing SpongeAPI, this is a serious issue!");
            }
        }

        return this.apiPlugin;
    }

    public final PluginContainer getCommonPlugin() {
        if (this.commonPlugin == null) {
            this.commonPlugin = this.pluginManager.plugin(PluginManager.SPONGE_PLUGIN_ID).orElse(null);

            if (this.commonPlugin == null) {
                throw new RuntimeException("Could not find the plugin representing Sponge, this is a serious issue!");
            }
        }

        return this.commonPlugin;
    }

    public abstract PluginContainer getPlatformPlugin();

    public final List<PluginContainer> getLauncherPlugins() {
        if (this.launcherPlugins.isEmpty()) {
            this.launcherPlugins.add(this.getMinecraftPlugin());
            this.launcherPlugins.add(this.getApiPlugin());
            this.launcherPlugins.add(this.getCommonPlugin());
            this.launcherPlugins.add(this.getPlatformPlugin());
        }

        return this.launcherPlugins;
    }

    protected void onLaunch() {
        this.createPlatformPlugins(this.pluginEngine);
    }

    protected abstract void createPlatformPlugins(final PluginEngine engine);

    public final void auditMixins() {
        MixinEnvironment.getCurrentEnvironment().audit();
    }

    public void loadPlugins() {
    }
}
