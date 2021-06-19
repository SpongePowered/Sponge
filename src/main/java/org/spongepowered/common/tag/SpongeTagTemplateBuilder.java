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

import com.google.common.collect.ImmutableMap;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.ResourceKeyed;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.RegistryKey;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.api.tag.TagTemplate;
import org.spongepowered.api.tag.TagType;
import org.spongepowered.api.tag.Taggable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class SpongeTagTemplateBuilder<T extends Taggable<T>> implements TagTemplate.Builder<T> {

    @Nullable
    private TagType<T> tagType;
    @Nullable
    private ResourceKey key;
    private boolean replace = false;
    private final Map<ResourceKey, Boolean> values = new HashMap<>();
    // ResourceKey to whether it is required.
    private final Map<ResourceKey, Boolean> children = new HashMap<>();

    @Override
    public <NT extends Taggable<NT>> TagTemplate.Builder<NT> type(final TagType<NT> tagType) {
        if (!this.values.isEmpty() || !children.isEmpty()) {
            throw new IllegalStateException("Cannot change tag type once values are added!");
        }
        this.tagType = null;
        final SpongeTagTemplateBuilder<NT> builder = (SpongeTagTemplateBuilder<NT>) this;
        builder.tagType = tagType;
        return builder;
    }

    @Override
    public TagTemplate.Builder<T> key(ResourceKey key) {
        this.key = key;
        return this;
    }

    @Override
    public TagTemplate.Builder<T> replace(final boolean replace) {
        this.ensureTagTypeAdded();
        this.replace = replace;
        return this;
    }

    @Override
    public TagTemplate.Builder<T> addChild(final RegistryKey<Tag<T>> childTag, boolean required) {
        this.ensureTagTypeAdded();
        if (this.tagType == null) {
            throw new IllegalStateException("Tag type must be set first!");
        }
        this.children.put(childTag.location(), required);
        return this;
    }

    @Override
    public TagTemplate.Builder<T> addChild(TagTemplate childTag) throws IllegalArgumentException {
        this.ensureTagTypeAdded();
        final SpongeTagTemplate spongeTagTemplate = ((SpongeTagTemplate) childTag);
        if (spongeTagTemplate.tagType() != this.tagType) {
            throw new IllegalArgumentException("TagTemplate given is not compatible with this builder, it is for a different type!");
        }
        // Circular references aren't allow - causes loading failure. So do a basic sanity check,
        // we can't ensure theres no circular references until everything is resolved, but we can check
        // 1 level down if we're given a tagtemplate.
        if (spongeTagTemplate.subTags().containsKey(this.key)) {
            throw new IllegalArgumentException("Recursive tag reference detected! Cannot add a child tag with parent as child! Key: " + this.key);
        }
        this.children.put(childTag.key(), true);
        return this;
    }

    @Override
    public TagTemplate.Builder<T> addChildren(final Collection<RegistryKey<Tag<T>>> children, final boolean required) {
        this.ensureTagTypeAdded();
        children.stream().map(RegistryKey::location).forEach(key -> this.children.put(key, required));
        return this;
    }

    @Override
    public TagTemplate.Builder<T> addChildren(Map<RegistryKey<Tag<T>>, Boolean> childrenMap) {
        childrenMap.forEach((k, v) -> this.children.put(k.location(), v));
        return this;
    }

    @Override
    public TagTemplate.Builder<T> addValue(final RegistryKey<T> value, final boolean required) throws IllegalArgumentException {
        this.ensureTagTypeAdded();
        this.values.put(value.location(), required);
        return this;
    }

    @Override
    public TagTemplate.Builder<T> addValues(final Collection<RegistryKey<T>> values, boolean required) throws IllegalArgumentException {
        this.ensureTagTypeAdded();
        values.forEach(v -> this.values.put(v.location(), required));
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
                ImmutableMap.copyOf(this.values),
                ImmutableMap.copyOf(this.children));
    }

    @Override
    public TagTemplate.Builder<T> reset() {
        this.tagType = null;
        this.replace = false;
        this.values.clear();
        this.children.clear();
        return this;
    }
}
