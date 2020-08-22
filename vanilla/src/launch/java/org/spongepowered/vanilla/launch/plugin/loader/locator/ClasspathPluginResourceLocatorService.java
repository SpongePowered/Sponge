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
import org.spongepowered.plugin.jvm.JVMConstants;
import org.spongepowered.plugin.jvm.locator.JVMPluginResource;
import org.spongepowered.plugin.jvm.locator.JVMPluginResourceLocatorService;
import org.spongepowered.plugin.jvm.locator.ResourceType;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public final class ClasspathPluginResourceLocatorService extends JVMPluginResourceLocatorService {

    private static final String NAME = "java_classpath";

    @Override
    public String getName() {
        return ClasspathPluginResourceLocatorService.NAME;
    }

    @Override
    public List<JVMPluginResource> locatePluginResources(final PluginEnvironment environment) {
        environment.getLogger().info("Locating '{}' resources...", this.getName());

        final List<JVMPluginResource> pluginFiles = new ArrayList<>();
        final Enumeration<URL> resources;
        try {
            resources = ClassLoader.getSystemClassLoader().getResources(JVMConstants.Manifest.LOCATION);
        } catch (final IOException e) {
            throw new RuntimeException("Failed to enumerate classloader resources!");
        }

        while (resources.hasMoreElements()) {
            final URL url = resources.nextElement();

            final URI uri;
            try {
                uri = url.toURI();
            } catch (final URISyntaxException e) {
                environment.getLogger().error("Malformed URL '{}'. Skipping...", url, e);
                continue;
            }
            final Path path;

            // Jars
            if (uri.getRawSchemeSpecificPart().contains("!")) {
                try {
                    path = Paths.get(new URI(uri.getRawSchemeSpecificPart().split("!")[0]));
                } catch (final URISyntaxException e) {
                    environment.getLogger().error("Malformed URI for Jar '{}. Skipping...", url, e);
                    continue;
                }

                try (final JarFile jf = new JarFile(path.toFile())) {
                    if (!this.isValidManifest(environment, jf.getManifest())) {
                        environment.getLogger().error("Manifest specified in '{}' is not valid. Skipping...", jf);
                        continue;
                    }
                    final JarEntry pluginMetadataJarEntry = jf.getJarEntry(this.getMetadataPath());
                    if (pluginMetadataJarEntry == null) {
                        environment.getLogger().debug("'{}' does not contain any plugin metadata so it is not a plugin. Skipping...", jf);
                        continue;
                    }
                    pluginFiles.add(new JVMPluginResource(this.getName(), ResourceType.JAR, path));
                } catch (final IOException e) {
                    environment.getLogger().error("Error reading '{}' as a Jar file. Skipping...", url, e);
                }
            } else {
                Manifest manifest;
                try (final InputStream stream = url.openStream()) {
                    manifest = new Manifest(stream);
                } catch (final IOException e) {
                    environment.getLogger().error("Malformed URL '{}' in locator '{}'. Skipping...", url, e);
                    continue;
                }

                if (!this.isValidManifest(environment, manifest)) {
                    environment.getLogger().error("Manifest specified in '{}' is not valid. Skipping...", url);
                    continue;
                }

                try {
                    path = Paths.get(new URI("file://" + uri.getRawSchemeSpecificPart().substring(0, uri.getRawSchemeSpecificPart().length() - (JVMConstants.Manifest.LOCATION.length()))));
                } catch (final URISyntaxException e) {
                    environment.getLogger().error("Error creating root URI for '{}'. Skipping...", url, e);
                    continue;
                }

                final Path pluginMetadataFile = path.resolve(this.getMetadataPath());
                if (Files.notExists(pluginMetadataFile)) {
                    environment.getLogger().debug("'{}' does not contain any plugin metadata so it is not a plugin. Skipping...", path);
                    continue;
                }
                pluginFiles.add(new JVMPluginResource(this.getName(), ResourceType.DIRECTORY, path));
            }
        }

        environment.getLogger().info("Located [{}] resource(s) for '{}'...", pluginFiles.size(), this.getName());

        return pluginFiles;
    }
}
