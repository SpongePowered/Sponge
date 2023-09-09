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
package org.spongepowered.forge.applaunch.loading.moddiscovery.locator;

import cpw.mods.jarhandling.SecureJar;
import net.minecraftforge.fml.loading.ClasspathLocatorUtils;
import net.minecraftforge.fml.loading.moddiscovery.AbstractJarFileModLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.common.applaunch.plugin.PluginPlatformConstants;
import org.spongepowered.forge.applaunch.loading.moddiscovery.ModFileParsers;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class ClasspathPluginLocator extends AbstractJarFileModLocator {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String PLUGINS_JSON = "META-INF/sponge_plugins.json";
    private Set<Path> modCoords;

    @Override
    public Stream<Path> scanCandidates() {
        return this.modCoords.stream();
    }

    @Override
    protected ModFileOrException createMod(Path... path) {
        return new ModFileOrException(ModFileParsers.newPluginInstance(SecureJar.from(path), this, PluginPlatformConstants.METADATA_FILE_NAME), null);
    }

    @Override
    public String name() {
        return "plugin classpath";
    }

    @Override
    public void initArguments(final Map<String, ?> arguments) {
        try {
            this.modCoords = new LinkedHashSet<>();
            this.locateMods(ClasspathPluginLocator.PLUGINS_JSON, "classpath_plugin", path -> true);
        } catch (IOException e) {
            ClasspathPluginLocator.LOGGER.fatal("Error trying to find resources", e);
            throw new RuntimeException("wha?", e);
        }
    }

    private void locateMods(final String resource, final String name, final Predicate<Path> filter) throws IOException {
        final Enumeration<URL> pluginJsons = ClassLoader.getSystemClassLoader().getResources(resource);
        while (pluginJsons.hasMoreElements()) {
            final URL url = pluginJsons.nextElement();
            final Path path = ClasspathLocatorUtils.findJarPathFor(resource, name, url);
            if (Files.isDirectory(path))
                continue;

            if (filter.test(path)) {
                ClasspathPluginLocator.LOGGER.debug("Found classpath plugin: {}", path);
                this.modCoords.add(path);
            }
        }
    }
}
