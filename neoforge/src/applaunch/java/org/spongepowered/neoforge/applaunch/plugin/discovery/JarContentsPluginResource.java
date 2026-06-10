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

import net.neoforged.fml.jarcontents.JarContents;
import net.neoforged.fml.jarmoduleinfo.JarModuleInfo;
import org.spongepowered.common.applaunch.plugin.discovery.SpongeJVMPluginResource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.module.ModuleDescriptor;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.jar.Manifest;

public final class JarContentsPluginResource implements SpongeJVMPluginResource {
    private final Path[] paths;
    private final JarContents jar;

    public JarContentsPluginResource(final Path[] paths) {
        Objects.requireNonNull(paths, "paths");
        if (paths.length == 0) {
            throw new IllegalArgumentException("Need at least one path");
        }
        this.paths = paths;
        try {
            this.jar = JarContents.ofPaths(Arrays.asList(paths));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public JarContentsPluginResource(final JarContents jar) {
        Objects.requireNonNull(jar, "jar");
        this.paths = jar.getContentRoots().toArray(new Path[0]);
        this.jar = jar;
    }

    public JarContents jar() {
        return this.jar;
    }

    @Override
    public Path path() {
        return this.jar.getPrimaryPath();
    }

    @Override
    public Optional<URI> locateResource(String path) {
        return this.jar.findFile(path);
    }

    @Override
    public Manifest manifest() {
        return this.jar.getManifest();
    }

    @Override
    public ModuleDescriptor module() {
        return JarModuleInfo.from(this.jar).createDescriptor(this.jar);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof JarContentsPluginResource that)) {
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
        return "JarContentsPluginResource[paths=" + Arrays.toString(this.paths) + "]";
    }
}
