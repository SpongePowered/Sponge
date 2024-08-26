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

import org.spongepowered.plugin.Environment;
import org.spongepowered.plugin.PluginResourceLocatorService;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public final class EnvironmentPluginResourceLocatorService implements PluginResourceLocatorService<SecureJarPluginResource> {

    @Override
    public String name() {
        return "environment_plugin";
    }

    @Override
    public Set<SecureJarPluginResource> locatePluginResources(Environment environment) {
        final Set<SecureJarPluginResource> resources = new HashSet<>();
        for (final Path[] paths : EnvironmentPluginResourceLocatorService.getPluginsPaths()) {
            resources.add(new SecureJarPluginResource(this.name(), paths));
        }
        return resources;
    }

    private static List<Path[]> getPluginsPaths() {
        final String env = System.getenv("SPONGE_PLUGINS");
        if (env == null) {
            return Collections.emptyList();
        }

        List<Path[]> plugins = new ArrayList<>();
        for (final String entry : env.split(";")) {
            if (entry.isBlank()) {
                continue;
            }
            plugins.add(Stream.of(entry.split("&")).map(Path::of).toArray(Path[]::new));
        }

        return plugins;
    }
}
