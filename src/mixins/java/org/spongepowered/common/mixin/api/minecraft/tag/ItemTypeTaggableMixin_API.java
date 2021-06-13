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
package org.spongepowered.common.mixin.api.minecraft.tag;

import net.minecraft.world.item.Item;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.api.tag.TagType;
import org.spongepowered.api.tag.TagTypes;
import org.spongepowered.api.tag.Taggable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.tag.TagUtil;

import java.util.Collection;

@Mixin(Item.class)
public abstract class ItemTypeTaggableMixin_API implements Taggable<ItemType> {

    @Override
    public TagType<ItemType> tagType() {
        return TagTypes.ITEM_TYPE.get();
    }

    @Override
    public Collection<Tag<ItemType>> tags() {
        return TagUtil.getAssociatedTags((ItemType) this, RegistryTypes.ITEM_TYPE_TAGS);
    }
}
