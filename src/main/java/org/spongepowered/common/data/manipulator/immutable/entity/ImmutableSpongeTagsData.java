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