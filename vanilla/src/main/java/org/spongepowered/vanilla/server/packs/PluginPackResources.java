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
package org.spongepowered.vanilla.server.packs;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.ResourcePackFileNotFoundException;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.plugin.PluginContainer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class PluginPackResources extends AbstractPackResources {

    private final String name;
    private final PluginContainer container;
    private final PackMetadataSection metadata;
    private final @Nullable Supplier<FileSystem> fileSystem;

    public PluginPackResources(final String name, final PluginContainer container, final @Nullable Supplier<FileSystem> fileSystem) {
        super(new File("sponge_plugin_pack"));
        this.name = name;
        this.container = container;
        this.metadata = new PackMetadataSection(new TextComponent("Plugin Resources"), 6);
        this.fileSystem = fileSystem;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    protected InputStream getResource(final String rawPath) throws IOException {
        return this.container.openResource(URI.create(rawPath)).orElseThrow(() -> new ResourcePackFileNotFoundException(this.file, rawPath));
    }

    @Override
    protected boolean hasResource(final String rawPath) {
        try {
            return this.container.locateResource(URI.create(rawPath)).isPresent();
        } catch (final Exception e) {
            return false;
        }
    }

    @Override
    public Collection<ResourceLocation> getResources(final PackType type, final String namespace, final String path, final int depth,
            final Predicate<String> fileNameValidator) {
        try {
            final Path root = this.typeRoot(type);
            return Files.walk(root.resolve(namespace).resolve(namespace), depth)
                .filter(s -> !s.getFileName().toString().endsWith(".mcmeta"))
                .map(Object::toString)
                .filter(fileNameValidator)
                .map(s -> new ResourceLocation(namespace, path))
                .collect(Collectors.toList());
        } catch (final IOException e) {
            return Collections.emptyList();
        }
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(final MetadataSectionSerializer<T> deserializer) throws IOException {
        if (deserializer.getMetadataSectionName().equals("pack")) {
            return (T) this.metadata;
        }
        return null;
    }

    @Override
    public Set<String> getNamespaces(final PackType type) {
        try {
            final @Nullable Path root = this.typeRoot(type);
            if (root != null) {
                return Files.list(root)
                        .map(Path::getFileName)
                        .map(Object::toString)
                        .filter(s -> {
                            if (s.equals(s.toLowerCase(Locale.ROOT))) {
                                return true;
                            } else {
                                SpongeCommon.logger().warn("Pack: ignored non-lowercased namespace: {} in {}", s,
                                        root.toAbsolutePath().toString());
                                return false;
                            }
                        })
                        .collect(Collectors.toSet());
            }
        } catch (final IOException e) {
            // ignored
        }
        return Collections.emptySet();
    }

    @Override
    public void close() {

    }

    private @Nullable Path typeRoot(final PackType type) throws IOException {
        if (this.fileSystem == null) {
            return null;
        }
        return this.fileSystem.get().getPath(type.getDirectory());
    }

}
