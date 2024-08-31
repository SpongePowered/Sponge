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
package org.spongepowered.vanilla.applaunch.plugin.resource;

import cpw.mods.jarhandling.JarMetadata;
import cpw.mods.jarhandling.SecureJar;
import org.spongepowered.plugin.PluginCandidate;
import org.spongepowered.plugin.builtin.jvm.JVMPluginResource;
import org.spongepowered.vanilla.applaunch.plugin.ResourceType;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.jar.Manifest;

public final class SecureJarPluginResource implements JVMPluginResource {
    private final String locator;
    private final SecureJar jar;

    private PluginJarMetadata pluginJarMetadata;
    private List<PluginCandidate<SecureJarPluginResource>> candidates;

    public SecureJarPluginResource(final String locator, final Path[] paths) {
        Objects.requireNonNull(locator, "locator");
        Objects.requireNonNull(paths, "paths");

        if (paths.length == 0) {
            throw new IllegalArgumentException("Need at least one path");
        }

        this.locator = locator;
        this.jar = SecureJar.from(jar -> {
            if (ResourceType.of(jar) == ResourceType.PLUGIN) {
                this.pluginJarMetadata = new PluginJarMetadata(jar, paths);
                this.candidates = new ArrayList<>();
                return this.pluginJarMetadata;
            }
            return JarMetadata.from(jar, paths);
        }, paths);
    }

    @Override
    public String locator() {
        return this.locator;
    }

    public SecureJar jar() {
        return this.jar;
    }

    @Override
    public Path path() {
        return this.jar.getPrimaryPath();
    }

    @Override
    public Manifest manifest() {
        return this.jar.moduleDataProvider().getManifest();
    }

    @Override
    public Path resourcesRoot() {
        return this.jar.getRootPath();
    }

    public void addCandidates(Collection<PluginCandidate<SecureJarPluginResource>> candidates) {
        if (this.candidates != null) {
            this.candidates.addAll(candidates);
        }
    }

    public void init() {
        if (this.pluginJarMetadata != null) {
            this.pluginJarMetadata.init(this.candidates.isEmpty() ? null : this.candidates.get(0).metadata());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SecureJarPluginResource that)) {
            return false;
        }
        return this.locator.equals(that.locator) && this.jar.equals(that.jar);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.locator, this.jar);
    }

    @Override
    public String toString() {
        return "SecureJarPluginResource[" +
                "locator=" + this.locator + ", " +
                "jar=" + this.jar + ']';
    }

}
