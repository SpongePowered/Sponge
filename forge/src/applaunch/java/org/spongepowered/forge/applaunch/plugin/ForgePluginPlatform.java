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
package org.spongepowered.forge.applaunch.plugin;

import net.minecraftforge.fml.loading.FMLPaths;
import org.spongepowered.common.applaunch.plugin.PluginPlatform;
import org.spongepowered.forge.applaunch.plugin.discovery.ForgePluginDiscovery;
import org.spongepowered.forge.applaunch.plugin.discovery.SecureJarPluginResource;
import org.spongepowered.plugin.builtin.jvm.JVMPluginResource;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ForgePluginPlatform extends PluginPlatform {
    private static final List<AutoCloseable> closeCallbacks = new ArrayList<>();

    private final ForgePluginDiscovery discovery;

    public ForgePluginPlatform() {
        this.discovery = new ForgePluginDiscovery(this.environment());
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
    public ForgePluginDiscovery discovery() {
        return this.discovery;
    }

    @Override
    public JVMPluginResource create(final Path[] paths) {
        return new SecureJarPluginResource(paths);
    }

    @Override
    public void addLoaderCloseCallback(final AutoCloseable closeable) {
        ForgePluginPlatform.closeCallbacks.add(closeable);
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (final AutoCloseable closeCallback : ForgePluginPlatform.closeCallbacks) {
                try {
                    closeCallback.close();
                } catch (Exception e) {
                    PluginPlatform.LOGGER.error("Failed to run close callback {}", closeCallback, e);
                }
            }
        }, "Sponge shutdown thread"));
    }
}
