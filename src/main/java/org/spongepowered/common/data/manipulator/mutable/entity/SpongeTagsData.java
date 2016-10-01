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
package org.spongepowered.common.data.manipulator.mutable.entity;

import java.util.HashSet;
import java.util.Set;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableTagsData;
import org.spongepowered.api.data.manipulator.mutable.entity.TagsData;
import org.spongepowered.api.data.value.mutable.SetValue;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeTagsData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongeSetValue;

public class SpongeTagsData extends AbstractData<TagsData, ImmutableTagsData> implements TagsData {

    private Set<String> tags;

    public SpongeTagsData() {
        this(new HashSet<>());
    }
    
    public SpongeTagsData(Set<String> tags) {
        super(TagsData.class);
        this.tags = tags;
        registerGettersAndSetters();
    }

    @Override
    public TagsData copy() {
        return new SpongeTagsData(this.tags);
    }

    @Override
    public ImmutableTagsData asImmutable() {
        return new ImmutableSpongeTagsData(new HashSet<>(this.tags));
    }

    @Override
    public SetValue<String> tags() {
        return new SpongeSetValue<>(Keys.TAGS, this.tags);
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.TAGS, () -> this.tags);
        registerFieldSetter(Keys.TAGS, tags -> this.tags = tags);
        registerKeyValue(Keys.TAGS, this::tags);
    }
    
    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
              .set(Keys.TAGS, new HashSet<>(this.tags));
    }

}
