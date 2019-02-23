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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Tag;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

public abstract class SpongeTagRegistry<T extends Tag<C>, C extends CatalogType> implements AlternateCatalogRegistryModule<T> {

    private final Map<CatalogKey, T> tags = new HashMap<>();
    private final TypeToken<C> targetType;
    private final Class<?> catalogClass;
    private final Set<ResourceLocation> alwaysAvailable = new HashSet<>(); // These should always be registered, even if a data pack deleted them

    private TagCollection<C> tagCollection;

    public SpongeTagRegistry(TypeToken<C> targetType, Class<?> catalogClass, TagCollection<C> tagCollection) {
        this.targetType = targetType;
        this.catalogClass = catalogClass;
        this.tagCollection = tagCollection;
        for (Field field : this.catalogClass.getFields()) {
            this.alwaysAvailable.add(new ResourceLocation(field.getName().toLowerCase()));
        }
    }

    TypeToken<C> getTargetType() {
        return this.targetType;
    }

    @Override
    public Map<String, T> provideCatalogMap() {
        final Map<String, T> mappings = new HashMap<>();
        for (ResourceLocation id : this.alwaysAvailable) {
            mappings.put(id.getPath(), this.tags.computeIfAbsent((CatalogKey) (Object) id, key -> {
                final net.minecraft.tags.Tag<C> mcTag = this.tagCollection.get(id);
                return createTag(id, mcTag, true);
            }));
        }
        return mappings;
    }

    @Override
    public Optional<T> get(CatalogKey key) {
        checkNotNull(key, "key");
        return Optional.ofNullable(this.tags.computeIfAbsent(key, key1 -> {
            final net.minecraft.tags.Tag<C> mcTag = this.tagCollection.get((ResourceLocation) (Object) key);
            if (mcTag == null) {
                return null;
            }
            return createTag(mcTag.getId(), mcTag);
        }));
    }

    @Override
    public Collection<T> getAll() {
        return this.tagCollection.getTagMap().values().stream()
                .map(mcTag -> this.tags.computeIfAbsent((CatalogKey) (Object) mcTag.getId(),
                        key -> createTag(mcTag.getId(), mcTag)))
                .collect(ImmutableList.toImmutableList());
    }

    final T createTag(ResourceLocation id, @Nullable net.minecraft.tags.Tag<C> mcTag) {
        return createTag(id, mcTag, this.alwaysAvailable.add(id));
    }

    protected abstract T createTag(ResourceLocation id, @Nullable net.minecraft.tags.Tag<C> mcTag, boolean alwaysAvailable);

    @Nullable
    net.minecraft.tags.Tag<C> getMcTag(ResourceLocation id, boolean createIfMissing) {
        if (createIfMissing) {
            return this.tagCollection.getOrCreate(id);
        } else {
            return this.tagCollection.get(id);
        }
    }

    public void updateCollection(TagCollection<C> tagCollection) {
        this.tagCollection = tagCollection;
        final Collection<ResourceLocation> newIds = tagCollection.getRegisteredTags();
        final Iterator<T> it = this.tags.values().iterator();
        while (it.hasNext()) {
            final SpongeTag tag = (SpongeTag) it.next();
            if (!tag.alwaysPresent && !newIds.contains(tag.getKey())) {
                it.remove();
            } else {
                tag.invalidate();
            }
        }
    }
}
