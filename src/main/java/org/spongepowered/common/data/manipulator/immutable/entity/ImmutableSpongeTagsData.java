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
package org.spongepowered.common.data.manipulator.immutable.entity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableTagsData;
import org.spongepowered.api.data.manipulator.mutable.entity.TagsData;
import org.spongepowered.api.data.value.immutable.ImmutableSetValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeTagsData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeSetValue;

public class ImmutableSpongeTagsData extends AbstractImmutableData<ImmutableTagsData, TagsData> implements ImmutableTagsData {

	private Set<String> tags;
	
	private ImmutableSetValue<String> tagsValue;
	
	public ImmutableSpongeTagsData(Set<String> tags)
	{
		super(ImmutableTagsData.class);
		this.tags = tags;
		this.tagsValue = new ImmutableSpongeSetValue<>(Keys.TAGS, tags);
		registerGetters();
	}
	
	public ImmutableSpongeTagsData() {
        this(Collections.<String>emptySet());
    }

	@Override
	public TagsData asMutable() {
		return new SpongeTagsData(new HashSet<>(this.tags));
	}

	@Override
	public ImmutableSetValue<String> tags() {
		return this.tagsValue;
	}

	@Override
	protected void registerGetters() {
		registerFieldGetter(Keys.TAGS, () -> this.tags);
		registerKeyValue(Keys.TAGS, this::tags);
	}
	
}