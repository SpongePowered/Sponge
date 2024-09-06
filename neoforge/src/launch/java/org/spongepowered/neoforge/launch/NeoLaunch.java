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
package org.spongepowered.neoforge.launch;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.moddiscovery.ModInfo;
import org.spongepowered.common.applaunch.plugin.PluginPlatform;
import org.spongepowered.common.inject.SpongeCommonModule;
import org.spongepowered.common.inject.SpongeModule;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.neoforge.applaunch.loading.metadata.PluginMetadataUtils;
import org.spongepowered.neoforge.launch.inject.SpongeNeoModule;
import org.spongepowered.neoforge.launch.plugin.NeoPluginManager;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.metadata.PluginMetadata;

import java.util.List;

public final class NeoLaunch extends Launch {

    private final NeoPluginManager pluginManager;
    private PluginContainer spongeNeoPlugin;

    public NeoLaunch(final PluginPlatform platform) {
        super(platform);
        this.pluginManager = new NeoPluginManager();
    }

    @Override
    public boolean dedicatedServer() {
        return FMLLoader.getDist() == Dist.DEDICATED_SERVER;
    }

    @Override
    public NeoPluginManager pluginManager() {
        return this.pluginManager;
    }

    @Override
    public Stage injectionStage() {
        return FMLLoader.isProduction() ? Stage.PRODUCTION : Stage.DEVELOPMENT;
    }

    @Override
    public Injector createInjector() {
        final List<Module> modules = Lists.newArrayList(
                new SpongeModule(),
                new SpongeCommonModule(),
                new SpongeNeoModule()
        );
        return Guice.createInjector(this.injectionStage(), modules);
    }

    @Override
    public PluginContainer platformPlugin() {
        if (this.spongeNeoPlugin == null) {
            this.spongeNeoPlugin = this.pluginManager().plugin("spongeneo").orElse(null);

            if (this.spongeNeoPlugin == null) {
                throw new RuntimeException("Could not find the plugin representing SpongeNeo, this is a serious issue!");
            }
        }

        return this.spongeNeoPlugin;
    }

    public PluginMetadata metadataForMod(final ModInfo info) {
        return PluginMetadataUtils.modToPlugin(info);
    }
}
