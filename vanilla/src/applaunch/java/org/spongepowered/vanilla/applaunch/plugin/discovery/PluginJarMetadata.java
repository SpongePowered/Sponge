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
package org.spongepowered.vanilla.applaunch.plugin.discovery;

import cpw.mods.jarhandling.JarMetadata;
import cpw.mods.jarhandling.SecureJar;
import org.spongepowered.plugin.metadata.PluginMetadata;

import java.lang.module.ModuleDescriptor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class PluginJarMetadata implements JarMetadata {
    private final ModuleDescriptor descriptor;

    public PluginJarMetadata(final SecureJar jar, final Path[] paths) {
        this.descriptor = PluginJarMetadata.standardDescriptor(jar, paths);
    }

    public PluginJarMetadata(final SecureJar jar, final PluginMetadata plugin) {
        this.descriptor = PluginJarMetadata.pluginDescriptor(jar, plugin);
    }

    @Override
    public String name() {
        return this.descriptor.name();
    }

    @Override
    public String version() {
        return this.descriptor.rawVersion().orElse(null);
    }

    @Override
    public ModuleDescriptor descriptor() {
        return this.descriptor;
    }

    private static ModuleDescriptor pluginDescriptor(final SecureJar jar, final PluginMetadata plugin) {
        return PluginJarMetadata.descriptorBuilder(jar, plugin.id()).version(plugin.version().toString()).build();
    }

    private static ModuleDescriptor standardDescriptor(final SecureJar jar, final Path[] paths) {
        if (jar.moduleDataProvider().findFile("module-info.class").isEmpty() && Arrays.stream(paths).allMatch(Files::isDirectory)) {
            return PluginJarMetadata.descriptorBuilder(jar, jar.getPackages().stream().sorted().findFirst().orElseThrow()).build();
        }
        return JarMetadata.from(jar, paths).descriptor();
    }

    private static ModuleDescriptor.Builder descriptorBuilder(final SecureJar jar, final String name) {
        final ModuleDescriptor.Builder builder = ModuleDescriptor.newAutomaticModule(name).packages(jar.getPackages());
        jar.getProviders().stream()
            .filter(p -> !p.providers().isEmpty())
            .forEach(p -> builder.provides(p.serviceName(), p.providers()));
        return builder;
    }
}
