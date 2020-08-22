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
package org.spongepowered.vanilla.launch.plugin.loader.locator;

import org.spongepowered.plugin.PluginEnvironment;
import org.spongepowered.plugin.PluginKeys;
import org.spongepowered.plugin.jvm.locator.JVMPluginResource;
import org.spongepowered.plugin.jvm.locator.JVMPluginResourceLocatorService;
import org.spongepowered.plugin.jvm.locator.ResourceType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

public final class DirectoryPluginResourceLocatorService extends JVMPluginResourceLocatorService {

    private static final String NAME = "java_directory";

    @Override
    public String getName() {
        return DirectoryPluginResourceLocatorService.NAME;
    }

    @Override
    public List<JVMPluginResource> locatePluginResources(final PluginEnvironment environment) {
        environment.getLogger().info("Locating '{}' resources...", this.getName());

        final List<JVMPluginResource> pluginFiles = new ArrayList<>();

        for (final Path pluginsDir : environment.getBlackboard().get(PluginKeys.PLUGIN_DIRECTORIES).orElseGet(Collections::emptyList)) {
            if (Files.notExists(pluginsDir)) {
                environment.getLogger().debug("Plugin directory '{}' does not exist for locator '{}'. Skipping...", pluginsDir, this.getName());
                continue;
            }
            try {
                for (final Path path : Files.walk(pluginsDir).collect(Collectors.toList())) {
                    if (!Files.isRegularFile(path) || !path.getFileName().toString().endsWith(".jar")) {
                        continue;
                    }
                    try (final JarFile jf = new JarFile(path.toFile())) {
                        final Manifest manifest = jf.getManifest();

                        if (!this.isValidManifest(environment, manifest)) {
                            environment.getLogger().error("Manifest specified in '{}' is not valid for locator '{}'. Skipping...", jf, this.getName());
                            continue;
                        }

                        final JarEntry pluginMetadataJarEntry = jf.getJarEntry(this.getMetadataPath());
                        if (pluginMetadataJarEntry == null) {
                            environment.getLogger().debug("'{}' does not contain any plugin metadata so it is not a plugin. Skipping...", jf);
                            continue;
                        }

                        pluginFiles.add(new JVMPluginResource(this.getName(), ResourceType.JAR, path));
                    } catch (final IOException e) {
                        environment.getLogger().error("Error reading '{}' as a Jar file when traversing directory resources for plugin discovery! Skipping...", path, e);
                    }
                }
            }
            catch (final IOException ex) {
                environment.getLogger().error("Error walking plugins directory {}", pluginsDir, ex);
            }
        }

        environment.getLogger().info("Located [{}] resource(s) for '{}'...", pluginFiles.size(), this.getName());

        return pluginFiles;
    }
}
