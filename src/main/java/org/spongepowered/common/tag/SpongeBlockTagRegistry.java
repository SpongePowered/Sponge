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

import com.google.common.reflect.TypeToken;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.block.BlockTag;
import org.spongepowered.api.block.BlockTags;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.registry.util.RegisterCatalog;

import javax.annotation.Nullable;

@RegisterCatalog(BlockTags.class)
public final class SpongeBlockTagRegistry extends SpongeTagRegistry<BlockTag, BlockType> {

    public static SpongeBlockTagRegistry get() {
        return Holder.INSTANCE;
    }

    SpongeBlockTagRegistry() {
        super(TypeToken.of(BlockType.class), BlockTags.class, (TagCollection) net.minecraft.tags.BlockTags.getCollection());
    }

    @Override
    protected BlockTag createTag(ResourceLocation id, @Nullable Tag<BlockType> mcTag, boolean alwaysAvailable) {
        return new SpongeBlockTag(id, mcTag, this, alwaysAvailable);
    }

    static final class Holder {

        static final SpongeBlockTagRegistry INSTANCE = new SpongeBlockTagRegistry();
    }
}
