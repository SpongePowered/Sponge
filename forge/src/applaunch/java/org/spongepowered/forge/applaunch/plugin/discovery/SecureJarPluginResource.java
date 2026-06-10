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

import cpw.mods.jarhandling.SecureJar;
import net.minecraftforge.fml.loading.moddiscovery.ModJarMetadata;
import org.spongepowered.common.applaunch.plugin.discovery.SpongeJVMPluginResource;
import org.spongepowered.plugin.metadata.PluginMetadata;

import java.lang.module.ModuleDescriptor;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.jar.Manifest;

public final class SecureJarPluginResource implements SpongeJVMPluginResource {
    private final Path[] paths;
    private final SecureJar jar;

    public SecureJarPluginResource(final Path[] paths) {
        Objects.requireNonNull(paths, "paths");
        if (paths.length == 0) {
            throw new IllegalArgumentException("Need at least one path");
        }
        this.paths = paths;
        this.jar = SecureJar.from(jar -> new PluginJarMetadata(jar, paths), paths);
    }

    public SecureJar jar() {
        return this.jar;
    }

    public SecureJar pluginJar(final PluginMetadata metadata) {
        return SecureJar.from(jar -> new PluginJarMetadata(jar, metadata), this.paths);
    }

    @SuppressWarnings("UnstableApiUsage")
    public SecureJar modJar(final ModJarMetadata metadata) {
        return SecureJar.from(jar -> metadata, this.paths);
    }

    @Override
    public Path path() {
        return this.jar.getPrimaryPath();
    }

    @Override
    public Optional<URI> locateResource(final String path) {
        return this.jar.moduleDataProvider().findFile(path);
    }

    @Override
    public Manifest manifest() {
        return this.jar.moduleDataProvider().getManifest();
    }

    @Override
    public ModuleDescriptor module() {
        return this.jar.moduleDataProvider().descriptor();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SecureJarPluginResource that)) {
            return false;
        }
        return Arrays.equals(this.paths, that.paths);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.paths);
    }

    @Override
    public String toString() {
        return "SecureJarPluginResource[paths=" + Arrays.toString(this.paths) + "]";
    }
}
