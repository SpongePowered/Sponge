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
package org.spongepowered.neoforge.applaunch.plugin.discovery;

import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import org.spongepowered.plugin.Environment;
import org.spongepowered.plugin.builtin.jvm.JVMPluginResource;
import org.spongepowered.plugin.discovery.PluginResourceLocator;
import org.spongepowered.plugin.discovery.UnknownResourceStrategy;

import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class SpongeNeoResourceLocator implements PluginResourceLocator {

    @Override
    public String name() {
        return "spongeneo";
    }

    @Override
    public Collection<Result> locatePluginResources(final Environment environment) throws Exception {
        final List<Result> resources = new LinkedList<>();
        if (!FMLEnvironment.isProduction()) {
            return resources;
        }

        // Official JiJ cannot be used in a service jar. We have to reimplement it.
        // FML 10 only supports files directly on disk, so we must extract them.
        final URL rootJar = SpongeNeoResourceLocator.class.getProtectionDomain().getCodeSource().getLocation();
        final Path cacheDir = FMLPaths.JIJ_CACHEDIR.get();

        try (final FileSystem fs = FileSystems.newFileSystem(new URI("jar:" + rootJar), Map.of())) {
            final Path jarsDir = fs.getPath("jars");

            // Difference with the official JiJ: our checksums are precalculated, to avoid unnecessary IO.
            for (final String line : Files.readAllLines(jarsDir.resolve("checksums.txt"))) {
                final int i = line.indexOf(' ');
                final String digest = line.substring(0, i), name = line.substring(i + 1);
                final Path target = cacheDir.resolve(digest).resolve(name);

                // FML does not verify the cached file checksum, so neither do we.
                // The checksum is only there to distinguish different files.
                if (!Files.exists(target)) {
                    Files.createDirectories(target.getParent());
                    Files.copy(jarsDir.resolve(name), target);
                }

                resources.add(new Result(JVMPluginResource.create(environment, target), UnknownResourceStrategy.WARN));
            }
        }

        return resources;
    }
}
