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

import com.google.inject.Stage;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLLoader;
import org.spongepowered.common.applaunch.plugin.PluginEngine;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.forge.launch.plugin.ForgePluginManager;
import org.spongepowered.plugin.PluginContainer;

public class ForgeLaunch extends Launch {
    private PluginContainer spongeForgePlugin;

    public ForgeLaunch(final PluginEngine engine) {
        super(engine, new ForgePluginManager());
    }

    @Override
    public boolean isVanilla() {
        return false;
    }

    @Override
    public boolean isDedicatedServer() {
        return FMLLoader.getDist() == Dist.DEDICATED_SERVER;
    }

    @Override
    public Stage getInjectionStage() {
        return FMLLoader.isProduction() ? Stage.PRODUCTION : Stage.DEVELOPMENT;
    }

    @Override
    public PluginContainer getPlatformPlugin() {
        if (this.spongeForgePlugin == null) {
            this.spongeForgePlugin = this.getPluginManager().plugin("spongeforge").orElse(null);

            if (this.spongeForgePlugin == null) {
                throw new RuntimeException("Could not find the plugin representing SpongeForge, this is a serious issue!");
            }
        }

        return this.spongeForgePlugin;
    }

    @Override
    protected void createPlatformPlugins(final PluginEngine engine) {
        // todo
    }
}
