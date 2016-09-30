package org.spongepowered.common.data.manipulator.mutable.entity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableTagsData;
import org.spongepowered.api.data.manipulator.mutable.entity.TagsData;
import org.spongepowered.api.data.value.mutable.SetValue;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeTagsData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongeSetValue;

public class SpongeTagsData extends AbstractData<TagsData, ImmutableTagsData> implements TagsData {

	private Set<String> tags;
	
	public SpongeTagsData(Set<String> tags)
	{
		super(TagsData.class);
		this.tags = tags;
		registerGettersAndSetters();
	}
	
	public SpongeTagsData() {
        this(Collections.<String>emptySet());
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
	
}
