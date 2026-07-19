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
package org.spongepowered.forge.applaunch.plugin.discovery;

import com.google.common.collect.ImmutableMap;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.spongepowered.plugin.Environment;
import org.spongepowered.plugin.builtin.jvm.JVMPluginResource;
import org.spongepowered.plugin.discovery.PluginResourceLocator;
import org.spongepowered.plugin.discovery.UnknownResourceStrategy;

import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.*;

public final class SpongeForgeResourceLocator implements PluginResourceLocator {

    @Override
    public String name() {
        return "spongeforge";
    }

    @Override
    public Collection<Result> locatePluginResources(final Environment environment) throws Exception {
        if (!FMLEnvironment.production) {
            return Collections.emptyList();
        }

        final URL rootJar = SpongeForgeResourceLocator.class.getProtectionDomain().getCodeSource().getLocation();
        final FileSystem fs = FileSystems.getFileSystem(rootJar.toURI()); // FML has already opened a file system for this jar
        return Files.list(fs.getPath("jars"))
            .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar"))
            .map(path -> {
                try {
                    URI jij = new URI("jij:" + path.toAbsolutePath().toUri().getRawSchemeSpecificPart()).normalize();
                    final Map<String, ?> env = ImmutableMap.of("packagePath", path);
                    FileSystem jijFS = FileSystems.newFileSystem(jij, env);
                    return jijFS.getPath("/"); // root of the archive to load
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            })
            .map((path) -> new Result(JVMPluginResource.create(environment, path), UnknownResourceStrategy.WARN))
            .toList();
    }
}
