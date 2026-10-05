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
package org.spongepowered.neoforge.applaunch.plugin;

import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import org.spongepowered.common.applaunch.plugin.PluginPlatform;
import org.spongepowered.neoforge.applaunch.plugin.discovery.JarContentsPluginResource;
import org.spongepowered.neoforge.applaunch.plugin.discovery.NeoPluginDiscovery;
import org.spongepowered.plugin.builtin.jvm.JVMPluginResource;

import java.nio.file.Path;

public final class NeoPluginPlatform extends PluginPlatform {
    private final NeoPluginDiscovery discovery;

    public NeoPluginPlatform() {
        this.discovery = new NeoPluginDiscovery(this.environment());
    }

    @Override
    public boolean vanilla() {
        return false;
    }

    @Override
    public Path baseDirectory() {
        return FMLPaths.GAMEDIR.get();
    }

    @Override
    public Path configDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public Path modsDirectory() {
        return FMLPaths.MODSDIR.get();
    }

    @Override
    public NeoPluginDiscovery discovery() {
        return this.discovery;
    }

    @Override
    public JVMPluginResource create(final Path[] paths) {
        return new JarContentsPluginResource(paths);
    }

    @Override
    public void addLoaderCloseCallback(final AutoCloseable closeable) {
        FMLLoader.getCurrent().addCloseCallback(closeable);
    }
}
