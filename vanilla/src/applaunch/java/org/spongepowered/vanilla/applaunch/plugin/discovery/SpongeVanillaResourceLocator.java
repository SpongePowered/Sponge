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

import org.spongepowered.plugin.Environment;
import org.spongepowered.plugin.builtin.jvm.JVMPluginResource;
import org.spongepowered.plugin.discovery.PluginResourceLocator;
import org.spongepowered.plugin.discovery.UnknownResourceStrategy;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class SpongeVanillaResourceLocator implements PluginResourceLocator {

    @Override
    public String name() {
        return "spongevanilla";
    }

    @Override
    public Collection<Result> locatePluginResources(final Environment environment) throws Exception {
        final String fsProp = System.getProperty("sponge.rootJarFS");
        if (fsProp == null) {
            return List.of();
        }
        final FileSystem fs = FileSystems.getFileSystem(new URI(fsProp));
        final Path spongeMod = newJarInJar(fs.getPath("jars", "spongevanilla-mod.jar"));
        return List.of(new Result(JVMPluginResource.create(environment, spongeMod), UnknownResourceStrategy.WARN));
    }

    private static Path newJarInJar(final Path jar) throws IOException, URISyntaxException {
        URI jij = new URI("jij:" + jar.toAbsolutePath().toUri().getRawSchemeSpecificPart()).normalize();
        final Map<String, ?> env = Map.of("packagePath", jar);
        FileSystem jijFS = FileSystems.newFileSystem(jij, env);
        return jijFS.getPath("/"); // root of the archive to load
    }
}
