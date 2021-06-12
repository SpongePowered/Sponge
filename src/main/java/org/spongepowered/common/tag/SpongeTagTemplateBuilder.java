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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.ResourceKeyed;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.api.tag.TagType;
import org.spongepowered.api.tag.Taggable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SpongeTagTemplateBuilder<T extends Taggable> implements Tag.Builder<T> {

    @Nullable
    private TagType<T> tagType;
    @Nullable
    private ResourceKey key;
    private boolean replace = false;
    private final Set<T> values = new HashSet<>();
    private final Set<Tag<T>> children = new HashSet<>();

    @Override
    public <NT extends Taggable> Tag.Builder<NT> type(final TagType<NT> tagType) {
        if (!this.values.isEmpty() || !children.isEmpty()) {
            throw new IllegalStateException("Cannot change tag type once values are added!");
        }
        this.tagType = null;
        final SpongeTagTemplateBuilder<NT> builder = (SpongeTagTemplateBuilder<NT>) this;
        builder.tagType = tagType;
        return builder;
    }

    @Override
    public Tag.Builder<T> key(ResourceKey key) {
        this.key = key;
        return this;
    }

    @Override
    public Tag.Builder<T> replace(final boolean replace) {
        this.ensureTagTypeAdded();
        this.replace = replace;
        return this;
    }

    @Override
    public Tag.Builder<T> addChild(final Tag<T> childTag) {
        ensureTagTypeAdded();
        if (this.tagType == null) {
            throw new IllegalStateException("Tag type must be set first!");
        }
        this.children.add(childTag);
        return this;
    }

    @Override
    public Tag.Builder<T> addChildren(final Collection<Tag<T>> children) {
        ensureTagTypeAdded();
        this.children.addAll(children);
        return this;
    }

    @Override
    public Tag.Builder<T> addValue(final T value) throws IllegalArgumentException {
        ensureTagTypeAdded();
        this.values.add(value);
        return this;
    }

    @Override
    public Tag.Builder<T> addValues(final Collection<T> values) throws IllegalArgumentException {
        ensureTagTypeAdded();
        this.values.addAll(values);
        return this;
    }

    private void ensureTagTypeAdded() {
        if (this.tagType == null) {
            throw new IllegalStateException("Tag type must be set first!");
        }
    }

    @Override
    public @NonNull SpongeTagTemplate build() {
        if (this.key == null) {
            throw new IllegalStateException("Key has not been provided yet!");
        }
        if (this.tagType == null) {
            throw new IllegalStateException("Tag type has not been provided yet!");
        }
        return new SpongeTagTemplate(this.key,
                this.tagType,
                this.replace,
                this.values.stream().map(v -> Sponge.game().registries().registry(this.tagType.taggableRegistry()).valueKey(v)).collect(Collectors.toList()),
                this.children.stream().map(ResourceKeyed::key).collect(Collectors.toList()));
    }

    @Override
    public Tag.Builder<T> reset() {
        this.tagType = null;
        this.replace = false;
        this.values.clear();
        this.children.clear();
        return this;
    }
}
