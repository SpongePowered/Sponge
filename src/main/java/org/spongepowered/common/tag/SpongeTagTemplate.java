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
import com.google.gson.JsonObject;
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
import java.util.Map;

public final class SpongeTagTemplate implements TagTemplate {

    private final ResourceKey key;
    private final TagType<@NonNull ?> tagType;
    private final boolean replace;
    private final Map<ResourceKey, Boolean> elements;
    private final Map<ResourceKey, Boolean> subTags;

    public SpongeTagTemplate(final ResourceKey key,
                             final TagType<@NonNull ?> tagType,
                             final boolean replace,
                             final Map<ResourceKey, Boolean> elements,
                             final Map<ResourceKey, Boolean> subTags) {
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
        return this.tagType;
    }

    public boolean replace() {
        return this.replace;
    }

    public Map<ResourceKey, Boolean> elements() {
        return ImmutableMap.copyOf(this.elements);
    }

    public Map<ResourceKey, Boolean> subTags() {
        return ImmutableMap.copyOf(this.subTags);
    }

    @Override
    public int contentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        final JsonObject jsonObject = this.toJson();
        try {
            final DataContainer container = DataFormats.JSON.get().read(jsonObject.getAsString());
            container.set(DataQuery.of("replace"), this.replace);
            return container;
        } catch (IOException e) {
            SpongeCommon.logger().error("Error reading json serialized by minecraft", e);
            return DataContainer.createNew();
        }
    }

    public JsonObject toJson() {
        final Tag.Builder builder = new Tag.Builder();
        this.elements.forEach((k, v) -> {
            final ResourceLocation location = (ResourceLocation) (Object) k;
            // "N/A" is supposed to be the source, but we don't know it, and we're serializing it so it isn't used anyway. (Gone when we serializeToJson)
            builder.add(v ? new Tag.ElementEntry(location) : new Tag.OptionalElementEntry(location), "N/A");
        });
        this.subTags.forEach((k, v) -> {
            final ResourceLocation location = (ResourceLocation) (Object) k;
            builder.add(v ? new Tag.TagEntry(location) : new Tag.OptionalTagEntry(location), "N/A");
        });
        return builder.serializeToJson();
    }

    @Override
    public DataPackType type() {
        return DataPackTypes.TAG;
    }
}
