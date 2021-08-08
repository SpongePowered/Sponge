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
package org.spongepowered.forge.launch;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import org.spongepowered.common.applaunch.plugin.PluginPlatform;
import org.spongepowered.common.inject.SpongeCommonModule;
import org.spongepowered.common.inject.SpongeModule;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.forge.applaunch.loading.metadata.PluginMetadataUtils;
import org.spongepowered.forge.launch.inject.SpongeForgeModule;
import org.spongepowered.forge.launch.plugin.ForgePluginManager;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.metadata.PluginMetadata;

import java.util.List;

public final class ForgeLaunch extends Launch {

    private final ForgePluginManager pluginManager;
    private PluginContainer spongeForgePlugin;

    public ForgeLaunch(final PluginPlatform platform) {
        super(platform);
        this.pluginManager = new ForgePluginManager();
    }

    @Override
    public boolean dedicatedServer() {
        return FMLLoader.getDist() == Dist.DEDICATED_SERVER;
    }

    @Override
    public ForgePluginManager pluginManager() {
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
                new SpongeForgeModule()
        );
        return Guice.createInjector(this.injectionStage(), modules);
    }

    @Override
    public PluginContainer platformPlugin() {
        if (this.spongeForgePlugin == null) {
            this.spongeForgePlugin = this.pluginManager().plugin("spongeforge").orElse(null);

            if (this.spongeForgePlugin == null) {
                throw new RuntimeException("Could not find the plugin representing SpongeForge, this is a serious issue!");
            }
        }

        return this.spongeForgePlugin;
    }

    public PluginMetadata metadataForMod(final ModInfo info) {
        return PluginMetadataUtils.modToPlugin(info);
    }
}
