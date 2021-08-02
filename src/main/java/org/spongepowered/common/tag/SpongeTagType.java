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
package org.spongepowered.common.tag;

import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.api.tag.TagType;
import org.spongepowered.api.tag.Taggable;

public final class SpongeTagType<T extends Taggable<T>> implements TagType<T> {

    private final String id;
    private final RegistryType<T> taggableRegistry;
    private final RegistryType<Tag<T>> tagRegistry;

    public SpongeTagType(String id, RegistryType<T> taggableRegistry, RegistryType<Tag<T>> tagRegistry) {
        this.id = id;
        this.taggableRegistry = taggableRegistry;
        this.tagRegistry = tagRegistry;
    }

    /**
     * The internal id for this tag type.
     * Used for the directory.
     *
     * @return This TagType's id
     */
    public String internalId() {
        return this.id;
    }

    @Override
    public RegistryType<T> taggableRegistry() {
        return this.taggableRegistry;
    }

    @Override
    public RegistryType<Tag<T>> tagRegistry() {
        return this.tagRegistry;
    }
}
