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
package org.spongepowered.common.resource;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.resource.ResourcePath;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class SpongeResourcePath implements ResourcePath {

    private final ResourceKey key;
    @Nullable private final ResourcePath parent;

    public SpongeResourcePath(final ResourceKey key) {
        this.key = key;
        final LinkedList<String> parts = this.pathParts(this.path());
        parts.removeLast();
        if (parts.isEmpty()) {
            this.parent = null;
        } else {
            this.parent = this.withPath(parts);
        }
    }

    @Override
    public ResourceKey key() {
        return this.key;
    }

    @Override
    public Optional<ResourcePath> parent() {
        return Optional.ofNullable(this.parent);
    }

    @Override
    public ResourcePath resolve(final String first, final String... children) {
        final List<String> parts = new LinkedList<>(this.pathParts(this.path()));
        parts.add(first);
        parts.addAll(Arrays.asList(children));
        return this.withPath(parts);
    }

    @Override
    public ResourcePath resolveSibling(final String sibling, final String... children) {
        final LinkedList<String> parts = this.pathParts(this.path());
        parts.removeLast();
        parts.add(sibling);
        parts.addAll(Arrays.asList(children));
        return this.withPath(parts);
    }

    @Override
    public String name() {
        return FilenameUtils.getName(this.path());
    }

    @Override
    public String baseName() {
        return FilenameUtils.getBaseName(this.path());
    }

    @Override
    public String extension() {
        return FilenameUtils.getExtension(this.path());
    }

    @Override
    public int compareTo(@NotNull final ResourcePath o) {
        return this.key.compareTo(o.key());
    }

    @Override
    public String toString() {
        return this.key.toString();
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SpongeResourcePath)) {
            return false;
        }

        final SpongeResourcePath that = (SpongeResourcePath) o;

        return this.key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return this.key.hashCode();
    }

    private ResourcePath withPath(final List<String> parts) {
        final String path = parts.stream()
            // replace windows separators with unix
            .map(FilenameUtils::separatorsToUnix)
            // split each part into more parts
            .flatMap(s -> this.pathParts(s).stream())
            // icky empty strings are icky
            .filter(Strings::isNotEmpty)
            // convert back to a string path
            .collect(Collectors.joining("/"));
        if (path.isEmpty()) {
            throw new IllegalArgumentException("Path is empty!");
        }
        try {
            return ResourcePath.of(this.namespace(), path);
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private LinkedList<String> pathParts(final String path) {
        return new LinkedList<>(Arrays.asList(path.split("/")));
    }

    public static final class FactoryImpl implements ResourcePath.Factory {

        @Override
        public ResourcePath of(final ResourceKey key) {
            return new SpongeResourcePath(Objects.requireNonNull(key, "key"));
        }
    }
}
