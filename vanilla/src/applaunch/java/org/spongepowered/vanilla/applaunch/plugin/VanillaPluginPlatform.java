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
package org.spongepowered.vanilla.applaunch.plugin;

import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IEnvironment;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.spongepowered.common.applaunch.plugin.PluginPlatform;
import org.spongepowered.plugin.builtin.jvm.JVMPluginResource;
import org.spongepowered.vanilla.applaunch.plugin.discovery.SecureJarPluginResource;
import org.spongepowered.vanilla.applaunch.plugin.discovery.VanillaPluginDiscovery;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class VanillaPluginPlatform extends PluginPlatform {
    private static final List<AutoCloseable> closeCallbacks = new ArrayList<>();

    private final VanillaPluginDiscovery discovery;
    private @MonotonicNonNull Path baseDirectory;

    public VanillaPluginPlatform() {
        this.discovery = new VanillaPluginDiscovery(this.environment());
    }

    @Override
    public boolean vanilla() {
        return true;
    }

    @Override
    public Path baseDirectory() {
        if (this.baseDirectory == null) {
            this.baseDirectory = Launcher.INSTANCE.environment().getProperty(IEnvironment.Keys.GAMEDIR.get()).orElse(Paths.get("."));
        }
        return this.baseDirectory;
    }

    @Override
    public Path configDirectory() {
        return this.baseDirectory().resolve("config");
    }

    @Override
    public Path modsDirectory() {
        return this.baseDirectory().resolve("mods");
    }

    @Override
    public VanillaPluginDiscovery discovery() {
        return this.discovery;
    }

    @Override
    public JVMPluginResource create(final Path[] paths) {
        return new SecureJarPluginResource(paths);
    }

    @Override
    public void addLoaderCloseCallback(final AutoCloseable closeable) {
        VanillaPluginPlatform.closeCallbacks.add(closeable);
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (final AutoCloseable closeCallback : VanillaPluginPlatform.closeCallbacks) {
                try {
                    closeCallback.close();
                } catch (Exception e) {
                    PluginPlatform.LOGGER.error("Failed to run close callback {}", closeCallback, e);
                }
            }
        }, "Sponge shutdown thread"));
    }
}
