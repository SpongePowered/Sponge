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
package org.spongepowered.common.plugin.discovery.spi;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.spongepowered.plugin.Environment;
import org.spongepowered.plugin.discovery.PluginMetadataReader;
import org.spongepowered.plugin.discovery.PluginResource;
import org.spongepowered.plugin.discovery.PluginResourceLocator;
import org.spongepowered.plugin.metadata.PluginMetadata;
import org.spongepowered.plugin.metadata.builtin.InheritableMetadata;
import org.spongepowered.plugin.metadata.builtin.StandardPluginMetadata;
import org.spongepowered.plugin.metadata.model.PluginLoaderSpecification;

import java.util.Collection;
import java.util.List;

public class DummyMetadataReader implements PluginMetadataReader {
    private static final VersionRange ANY_VERSION;

    static {
        try {
            ANY_VERSION = VersionRange.createFromVersionSpec("*");
        } catch (InvalidVersionSpecificationException e) {
            throw new RuntimeException(e);
        }
    }

    public static final PluginMetadata DUMMY_METADATA = StandardPluginMetadata.builder().id("dummy_plugin")
        .global(InheritableMetadata.builder().loader(new PluginLoaderSpecification("dummy_loader", ANY_VERSION)).version(new DefaultArtifactVersion("1.0.0")).build()).build();

    @Override
    public Collection<? extends PluginMetadata> readPluginMetadata(final Environment environment, final PluginResource resource, final List<PluginResourceLocator> locators) {
        if (resource instanceof DummyPluginResource) {
            return List.of(DummyMetadataReader.DUMMY_METADATA);
        }
        return List.of();
    }

    @Override
    public String name() {
        return "dummy_reader";
    }
}
