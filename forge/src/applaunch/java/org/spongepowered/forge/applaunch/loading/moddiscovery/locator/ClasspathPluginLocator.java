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

import com.google.common.base.Predicates;
import net.minecraftforge.fml.Logging;
import net.minecraftforge.fml.loading.LibraryFinder;
import net.minecraftforge.fml.loading.moddiscovery.AbstractJarFileLocator;
import net.minecraftforge.forgespi.locating.IModFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.forge.applaunch.loading.moddiscovery.ModFileParsers;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class ClasspathPluginLocator extends AbstractJarFileLocator {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String PLUGINS_JSON = "META-INF/plugins.json";
    private Set<Path> modCoords;

    @Override
    public List<IModFile> scanMods() {
        return this.modCoords.stream().
                map(mc -> ModFileParsers.newPluginInstance(mc, this, "plugins")).
                peek(f -> this.modJars.compute(f, (mf, fs)->createFileSystem(mf))).
                collect(Collectors.toList());
    }

    @Override
    public String name() {
        return "plugin classpath";
    }

    @Override
    public void initArguments(final Map<String, ?> arguments) {
        try {
            this.modCoords = new LinkedHashSet<>();
            this.locateMods(ClasspathPluginLocator.PLUGINS_JSON, "classpath_plugin", Predicates.alwaysTrue());
        } catch (IOException e) {
            ClasspathPluginLocator.LOGGER.fatal(Logging.CORE,"Error trying to find resources", e);
            throw new RuntimeException("wha?", e);
        }
    }

    private void locateMods(final String resource, final String name, final Predicate<Path> filter) throws IOException {
        final Enumeration<URL> pluginJsons = ClassLoader.getSystemClassLoader().getResources(resource);
        while (pluginJsons.hasMoreElements()) {
            final URL url = pluginJsons.nextElement();
            final Path path = LibraryFinder.findJarPathFor(resource, name, url);
            if (Files.isDirectory(path))
                continue;

            if (filter.test(path)) {
                ClasspathPluginLocator.LOGGER.debug(Logging.CORE, "Found classpath plugin: {}", path);
                this.modCoords.add(path);
            }
        }
    }
}
