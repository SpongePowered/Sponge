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

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.apache.commons.io.FilenameUtils;
import org.spongepowered.api.resource.ResourcePath;
import org.spongepowered.api.resource.ResourcePathException;

import java.util.LinkedList;
import java.util.List;

public interface ISpongeResourcePath extends ResourcePath {

    Splitter PATH_SPLITTER = Splitter.onPattern("[" + SEPARATOR + "\\\\]+");
    Joiner PATH_JOINER = Joiner.on(SEPARATOR);

    @Override
    default ResourcePath getParent() throws ResourcePathException {
        LinkedList<String> parts = new LinkedList<>(getPathParts());
        parts.removeLast();
        return ResourcePath.of(getNamespace(), PATH_JOINER.join(parts));
    }

    @Override
    default ResourcePath resolve(String... children) throws ResourcePathException {
        List<String> parts = new LinkedList<>(getPathParts());
        for (String c : children) {
            parts.addAll(PATH_SPLITTER.splitToList(c));
        }
        return ResourcePath.of(getNamespace(), PATH_JOINER.join(parts));
    }

    @Override
    default ResourcePath resolveSibling(String sibling) throws ResourcePathException {
        LinkedList<String> parts = new LinkedList<>(getPathParts());
        parts.removeLast();
        parts.addAll(PATH_SPLITTER.splitToList(sibling));
        return ResourcePath.of(getNamespace(), PATH_JOINER.join(parts));
    }

    @Override
    default List<String> getPathParts() {
        return PATH_SPLITTER.splitToList(getPath());
    }

    @Override
    default String getParentPath() {
        LinkedList<String> parts = new LinkedList<>(getPathParts());
        parts.removeLast();
        return PATH_JOINER.join(parts);
    }

    @Override
    default String getName() {
        return FilenameUtils.getName(getPath());
    }

    @Override
    default String getBaseName() {
        return FilenameUtils.getBaseName(getPath());
    }

    @Override
    default String getExtension() {
        return FilenameUtils.getExtension(getPath());
    }
}
