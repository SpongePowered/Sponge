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
package org.spongepowered.server.plugin;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import org.spongepowered.common.plugin.AbstractPluginContainer;
import org.spongepowered.plugin.meta.PluginDependency;
import org.spongepowered.plugin.meta.PluginMetadata;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class MetaPluginContainer extends AbstractPluginContainer {

    private final String id;

    private final String name;
    private final Optional<String> version;
    private final Optional<String> description;
    private final Optional<String> url;
    private final ImmutableList<String> authors;

    private final ImmutableBiMap<String, PluginDependency> dependencies;

    private final Optional<Path> source;

    public MetaPluginContainer(PluginMetadata metadata, Optional<Path> source) {
        checkNotNull(metadata, "metadata");

        this.id = metadata.getId();

        this.name = MoreObjects.firstNonNull(metadata.getName(), this.id);
        this.version = Optional.ofNullable(metadata.getVersion());
        this.description = Optional.ofNullable(metadata.getDescription());
        this.url = Optional.ofNullable(metadata.getUrl());
        this.authors = ImmutableList.copyOf(metadata.getAuthors());

        this.dependencies = ImmutableBiMap.copyOf(metadata.getDependenciesById());

        this.source = checkNotNull(source, "source");
    }

    @Override
    public final String getId() {
        return this.id;
    }

    @Override
    public final String getName() {
        return this.name;
    }

    @Override
    public final Optional<String> getVersion() {
        return this.version;
    }

    @Override
    public final Optional<String> getDescription() {
        return this.description;
    }

    @Override
    public final Optional<String> getUrl() {
        return this.url;
    }

    @Override
    public final List<String> getAuthors() {
        return this.authors;
    }

    @Override
    public final Set<PluginDependency> getDependencies() {
        return this.dependencies.values();
    }

    @Override
    public final Optional<PluginDependency> getDependency(String id) {
        return Optional.ofNullable(this.dependencies.get(id));
    }

    @Override
    public Optional<Path> getSource() {
        return this.source;
    }

}
