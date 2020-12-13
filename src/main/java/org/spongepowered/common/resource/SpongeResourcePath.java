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

import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.util.Strings;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.resource.ResourcePath;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Wrapper class for {@link ResourceKey} with additional methods related to
 * paths.
 */
public class SpongeResourcePath implements ResourcePath {
    private final ResourceKey key;

    public SpongeResourcePath(ResourceKey key) {
        this.key = key;
    }

    @Override
    public ResourceKey key() {
        return this.key;
    }

    private ResourcePath withPath(List<String> parts) {
        String path = parts.stream()
                // replace windows separators with unix
                .map(FilenameUtils::separatorsToUnix)
                // split each part into more parts
                .flatMap(s -> getPathParts(s).stream())
                // icky empty strings are icky
                .filter(Strings::isNotEmpty)
                // convert back to a string path
                .collect(Collectors.joining(SEPARATOR));
        if (path.isEmpty()) {
            throw new IllegalArgumentException("Path is empty!");
        }
        try {
            return ResourcePath.of(getNamespace(), path);
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private LinkedList<String> getPathParts(String path) {
        return new LinkedList<>(Arrays.asList(path.split(SEPARATOR)));
    }

    @Override
    public ResourcePath getParent() {
        LinkedList<String> parts = getPathParts(getPath());
        parts.removeLast();
        if (parts.isEmpty()) {
            throw new IllegalStateException("Path has no parent");
        }
        return withPath(parts);
    }

    @Override
    public ResourcePath resolve(String first, String... children) {
        List<String> parts = new LinkedList<>(getPathParts(getPath()));
        parts.add(first);
        parts.addAll(Arrays.asList(children));
        return withPath(parts);
    }

    @Override
    public ResourcePath resolveSibling(String sibling, String... children) {
        LinkedList<String> parts = getPathParts(getPath());
        parts.removeLast();
        parts.add(sibling);
        parts.addAll(Arrays.asList(children));
        return withPath(parts);
    }

    @Override
    public String getName() {
        return FilenameUtils.getName(getPath());
    }

    @Override
    public String getBaseName() {
        return FilenameUtils.getBaseName(getPath());
    }

    @Override
    public String getExtension() {
        return FilenameUtils.getExtension(getPath());
    }

    @Override
    public String asString() {
        return key().asString();
    }

    @Override
    public String toString() {
        return key().toString();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpongeResourcePath that = (SpongeResourcePath) o;

        return key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public int compareTo(ResourcePath o) {
        return key().compareTo(o.key());
    }

    public static ResourceLocation toVanilla(ResourcePath path) {
        return (ResourceLocation) (Object) path.key();
    }

    public static SpongeResourcePath fromVanilla(ResourceLocation location) {
        return new SpongeResourcePath((ResourceKey) (Object) location);
    }
}
