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
import net.minecraftforge.forgespi.Environment;
import org.spongepowered.common.launch.Launcher;
import org.spongepowered.common.launch.plugin.DummyPluginContainer;
import org.spongepowered.forge.launch.plugin.ForgePluginManager;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.metadata.PluginMetadata;
import org.spongepowered.plugin.metadata.util.PluginMetadataHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

public class ForgeLauncher extends Launcher {

    private final Stage injectionStage;
    private PluginContainer forgePlugin;

    protected ForgeLauncher(final Stage injectionStage) {
        super(new ForgePluginManager());
        this.injectionStage = injectionStage;
    }

    @Override
    public boolean isVanilla() {
        return false;
    }

    @Override
    public boolean isDedicatedServer() {
        return Environment.get().getDist().isDedicatedServer();
    }

    @Override
    public Stage getInjectionStage() {
        return this.injectionStage;
    }

    @Override
    public PluginContainer getPlatformPlugin() {
        if (this.forgePlugin == null) {
            this.forgePlugin = this.getPluginManager().getPlugin("spongeforge").orElse(null);

            if (this.forgePlugin == null) {
                throw new RuntimeException("Could not find the plugin representing SpongeVanilla, this is a serious issue!");
            }
        }

        return this.forgePlugin;
    }

    @Override
    protected void createPlatformPlugins(final Path gameDirectory) {
        try {
            final Collection<PluginMetadata> read = PluginMetadataHelper.builder().build()
                    .read(ForgeLauncher.class.getResourceAsStream("/plugins.json"));
            for (final PluginMetadata metadata : read) {
                this.getPluginManager().addPlugin(new DummyPluginContainer(metadata, gameDirectory, this.getLogger(), this));
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not load metadata information for the implementation! This should be impossible!");
        }
    }

}
