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

import net.minecraft.ResourceLocationException;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;
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
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PluginPackResources extends AbstractPackResources {

    private final PluginContainer container;
    private final PackMetadataSection metadata;
    private final @Nullable Supplier<FileSystem> fileSystem;
    private final File file;

    public PluginPackResources(final PackLocationInfo info, final PluginContainer container, final @Nullable Supplier<FileSystem> fileSystem) {
        super(info);
        this.file = new File("sponge_plugin_pack");
        this.container = container;
        this.metadata = new PackMetadataSection(Component.literal("Plugin Resources"), 6, Optional.empty());
        this.fileSystem = fileSystem;
    }

    @Override
    public IoSupplier<InputStream> getRootResource(final String... var1) {
        final String rawPath = String.join("/", var1);
        return this.getResource(rawPath);
    }

    private IoSupplier<InputStream> getResource(final String rawPath) {
        final Optional<URI> uri = this.container.locateResource(URI.create(rawPath));
        if (uri.isEmpty()) {
            return null;
        }
        return () -> uri.get().toURL().openStream();
    }

    @Override
    public IoSupplier<InputStream> getResource(final PackType type, final ResourceLocation loc) {
        return this.getResource(String.format(Locale.ROOT, "%s/%s/%s", type.getDirectory(), loc.getNamespace(), loc.getPath()));
    }

    @Override
    public void listResources(final PackType type, final String namespace, final String path, final ResourceOutput out) {
        try {
            final Path root = this.typeRoot(type);
            final Path namespaceDir = root.resolve(namespace).toAbsolutePath();
            try (final Stream<Path> stream = Files.walk(namespaceDir)) {
                stream.filter(Files::isRegularFile)
                        .filter(s -> !s.getFileName().toString().endsWith(".mcmeta"))
                        .map(namespaceDir::relativize)
                        .map(Object::toString)
// TODO filter needed?                   .filter(p -> filterValidPath(namespace, p, fileNameValidator))
                        .map(s -> new ResourceLocation(namespace, s))
                        .forEach(loc -> {
                            out.accept(loc, this.getResource(type, loc));
                        });
            }
        } catch (final IOException ignored) {
        }
    }

    private boolean filterValidPath(final String namespace, final String path, final Predicate<ResourceLocation> fileNameValidator) {
        try {
            final ResourceLocation loc = ResourceLocation.tryBuild(namespace, path);
            if (loc == null) {
                // LOGGER.warn("Invalid path in datapack: {}:{}, ignoring", $$1, $$7);
                return false;
            }
            return fileNameValidator.test(loc);
        } catch (ResourceLocationException e) {
            // LOGGER.error(var13.getMessage());
            return false;
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
                try (final Stream<Path> stream = Files.list(root)) {
                    return stream.map(Path::getFileName)
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
