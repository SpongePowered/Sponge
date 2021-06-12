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

import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.datapack.DataPackType;
import org.spongepowered.api.datapack.DataPackTypes;
import org.spongepowered.api.tag.TagTemplate;
import org.spongepowered.api.tag.TagType;
import org.spongepowered.common.SpongeCommon;

import java.io.IOException;
import java.util.List;

public class SpongeTagTemplate implements TagTemplate {

    private final ResourceKey key;
    private final TagType<@NonNull ?> tagType;
    private final boolean replace;
    private final List<ResourceKey> elements;
    private final List<ResourceKey> subTags;

    public SpongeTagTemplate(final ResourceKey key, final TagType<@NonNull ?> tagType, final boolean replace, final List<ResourceKey> elements, final List<ResourceKey> subTags) {
        this.key = key;
        this.tagType = tagType;
        this.replace = replace;
        this.elements = elements;
        this.subTags = subTags;
    }

    @Override
    public ResourceKey key() {
        return this.key;
    }

    public TagType<@NonNull ?> tagType() {
        return tagType;
    }

    public boolean replace() {
        return this.replace;
    }

    public List<ResourceKey> elements() {
        return ImmutableList.copyOf(this.elements);
    }

    public List<ResourceKey> subTags() {
        return ImmutableList.copyOf(this.subTags);
    }

    @Override
    public int contentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        final Tag.Builder builder = new Tag.Builder();
        for (final ResourceKey element : this.elements) {
            builder.addElement((ResourceLocation) (Object) element, this.key.namespace());
        }
        for (final ResourceKey tag : this.subTags) {
            builder.addTag((ResourceLocation) (Object) tag, this.key.namespace());
        }
        try {
            final DataContainer container = DataFormats.JSON.get().read(builder.serializeToJson().getAsString());
            container.set(DataQuery.of("replace"), this.replace);
            return container;
        } catch (IOException e) {
            SpongeCommon.getLogger().error("Error reading json serialized by minecraft", e);
            return DataContainer.createNew();
        }
    }

    @Override
    public DataPackType type() {
        return DataPackTypes.TAG;
    }
}
