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
package org.spongepowered.vanilla.applaunch.plugin.locator;

import cpw.mods.jarhandling.JarMetadata;
import cpw.mods.jarhandling.SecureJar;
import org.spongepowered.plugin.metadata.PluginMetadata;

import java.lang.module.ModuleDescriptor;
import java.nio.file.Path;

public class PluginJarMetadata implements JarMetadata {
    private final SecureJar jar;
    private final Path[] paths;

    private boolean initialized = false;
    private PluginMetadata plugin;
    private JarMetadata fallback;

    private ModuleDescriptor descriptor;

    public PluginJarMetadata(final SecureJar jar, final Path[] paths) {
        this.jar = jar;
        this.paths = paths;
    }

    public void init(final PluginMetadata plugin) {
        if (this.initialized) {
            throw new IllegalStateException("Metadata already initialized");
        }
        this.initialized = true;
        if (plugin == null) {
            this.fallback = JarMetadata.from(this.jar, this.paths);
        } else {
            this.plugin = plugin;
        }
    }

    private void checkInitialized() {
        if (!this.initialized) {
            throw new IllegalStateException("Metadata not initialized");
        }
    }

    @Override
    public String name() {
        checkInitialized();
        if (this.plugin == null) {
            return this.fallback.name();
        }
        return this.plugin.id();
    }

    @Override
    public String version() {
        checkInitialized();
        if (this.plugin == null) {
            return this.fallback.version();
        }
        return this.plugin.version().toString();
    }

    @Override
    public ModuleDescriptor descriptor() {
        checkInitialized();
        if (this.plugin == null) {
            return this.fallback.descriptor();
        }
        if (this.descriptor == null) {
            ModuleDescriptor.Builder builder = ModuleDescriptor.newAutomaticModule(name())
                    .version(version())
                    .packages(this.jar.getPackages());
            this.jar.getProviders().stream()
                    .filter(p -> !p.providers().isEmpty())
                    .forEach(p -> builder.provides(p.serviceName(), p.providers()));
            this.descriptor = builder.build();
        }
        return this.descriptor;
    }
}
