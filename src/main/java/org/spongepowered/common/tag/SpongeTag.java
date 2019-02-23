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

import com.google.common.reflect.TypeToken;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Tag;
import org.spongepowered.common.interfaces.tag.IMixinTagEntry;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.StreamSupport;

import javax.annotation.Nullable;

public abstract class SpongeTag<C extends CatalogType> implements Tag<C> {

    @Nullable private net.minecraft.tags.Tag<C> mcTag;

    private final ResourceLocation id;
    private final SpongeTagRegistry<? extends Tag<C>, C> registry;
    final boolean alwaysPresent;

    protected SpongeTag(ResourceLocation id, @Nullable net.minecraft.tags.Tag<C> mcTag,
            SpongeTagRegistry<? extends Tag<C>, C> registry, boolean alwaysPresent) {
        this.alwaysPresent = alwaysPresent;
        this.registry = registry;
        this.mcTag = mcTag;
        this.id = id;
    }

    private net.minecraft.tags.Tag<C> getMcTag() {
        if (this.mcTag == null) {
            this.mcTag = this.registry.getMcTag(this.id, this.alwaysPresent);
        }
        return this.mcTag;
    }

    void invalidate() {
        this.mcTag = null;
    }

    @Override
    public TypeToken<C> getTargetType() {
        return this.registry.getTargetType();
    }

    @Override
    public boolean contains(C catalog) {
        checkNotNull(catalog, "catalog");
        final net.minecraft.tags.Tag<C> tag = getMcTag();
        return tag != null && tag.contains(catalog);
    }

    @Override
    public boolean containsAll(Iterable<C> iterable) {
        checkNotNull(iterable, "iterable");
        final net.minecraft.tags.Tag<C> tag = getMcTag();
        if (tag == null) {
            return false;
        }
        final Collection<C> collection = tag.getAllElements();
        if (iterable instanceof Collection) {
            return collection.containsAll((Collection<?>) iterable);
        }
        return StreamSupport.stream(iterable.spliterator(), false).allMatch(collection::contains);
    }

    @Override
    public boolean contains(Tag<C> tag) {
        checkNotNull(tag, "tag");
        final net.minecraft.tags.Tag<C> mcTag = getMcTag();
        if (mcTag == null) {
            return false;
        }
        return containsMcTag(mcTag, ((SpongeTag<C>) tag).mcTag);
    }

    private static <C> boolean containsMcTag(net.minecraft.tags.Tag<C> parent, net.minecraft.tags.Tag<C> possibleChild) {
        for (net.minecraft.tags.Tag.ITagEntry<C> entry : parent.getEntries()) {
            if (entry instanceof net.minecraft.tags.Tag.TagEntry) {
                final IMixinTagEntry<C> mixinTagEntry = (IMixinTagEntry<C>) entry;
                final net.minecraft.tags.Tag<C> tag = mixinTagEntry.getTag();
                if (tag != null) {
                    if (possibleChild == tag) {
                        return true;
                    } else if (containsMcTag(tag, possibleChild)) {
                        return true;
                    }
                } else if (((net.minecraft.tags.Tag.TagEntry) entry).getSerializedId().equals(possibleChild.getId())){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Collection<C> getAll() {
        final net.minecraft.tags.Tag<C> tag = getMcTag();
        if (tag == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableCollection(tag.getAllElements());
    }

    @Override
    public CatalogKey getKey() {
        return (CatalogKey) (Object) this.id;
    }
}
