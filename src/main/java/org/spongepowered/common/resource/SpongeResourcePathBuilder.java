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
import net.minecraft.util.ResourceLocationException;
import org.apache.commons.io.FilenameUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.resource.ResourcePath;
import org.spongepowered.api.resource.ResourcePathException;
import org.spongepowered.plugin.PluginContainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class SpongeResourcePathBuilder implements ResourcePath.Builder {

    @Nullable
    private String namespace;
    private final List<String> pathParts = new ArrayList<>();

    @Override
    public ResourcePath.Builder namespace(String namespace) {
        checkNotNull(namespace, "Namespace cannot be null");
        this.namespace = namespace;
        return this;
    }

    @Override
    public ResourcePath.Builder namespace(PluginContainer container) {
        checkNotNull(container, "PluginContainer cannot be null");
        this.namespace = container.getMetadata().getId();
        return this;
    }

    private void addPath(String path) {
        checkNotNull(path, "Path cannot be null");
        pathParts.addAll(Arrays.asList(path.split(ResourcePath.SEPARATOR)));
    }

    @Override
    public ResourcePath.Builder path(String path) {
        checkNotNull(path, "Value cannot be null");
        pathParts.clear();
        addPath(path);
        return this;
    }

    @Override
    public ResourcePath.Builder paths(String... paths) {
        checkNotNull(paths, "Paths cannot be null");
        return paths(Arrays.asList(paths));
    }

    @Override
    public ResourcePath.Builder paths(Collection<String> paths) {
        checkNotNull(paths, "Paths cannot be null");
        for (String path : paths) {
            checkNotNull(path, "Elements in array cannot be null");
            addPath(path);
        }

        return this;
    }

    @Override
    public ResourcePath build() {
        List<String> parts = this.pathParts.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());
        checkState(parts.isEmpty(), "Path cannot be empty");

        // Automatically normalize the path.
        String joinedPath = String.join(ResourcePath.SEPARATOR, parts);
        String path = FilenameUtils.normalize(joinedPath, true);
        if (path == null) {
            throw new ResourcePathException("Could not normalize path: " + joinedPath);
        }
        try {
            final ResourceLocation resourceLocation;
            if (this.namespace != null) {
                resourceLocation = new ResourceLocation(this.namespace, path);
            } else {
                resourceLocation = new ResourceLocation(path);
            }
            return (ResourcePath) (Object) resourceLocation;
        } catch (ResourceLocationException e) {
            throw new ResourcePathException(e);
        }
    }

    @Override
    public ResourcePath.Builder reset() {
        this.namespace = null;
        this.pathParts.clear();
        return this;
    }
}
