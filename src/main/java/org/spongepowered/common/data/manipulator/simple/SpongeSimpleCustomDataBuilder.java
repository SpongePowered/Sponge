package org.spongepowered.common.data.manipulator.simple;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.data.manipulator.SimpleCustomData;
import org.spongepowered.api.data.persistence.DataContentUpdater;

import java.util.Set;

public class SpongeSimpleCustomDataBuilder implements SimpleCustomData.Builder {

    protected int version;
    protected Set<DataContentUpdater> updaters;
    protected String name;
    protected String id;

    public SpongeSimpleCustomDataBuilder() {
        reset();
    }

    @Override
    public SpongeSimpleCustomDataBuilder contentVersion(int version) {
        this.version = version;
        return this;
    }

    @Override
    public SpongeSimpleCustomDataBuilder contentUpdaters(Iterable<DataContentUpdater> updaters) {
        this.updaters = ImmutableSet.copyOf(checkNotNull(updaters));
        return this;
    }

    @Override
    public SpongeSimpleCustomDataBuilder name(String name) {
        this.name = checkNotNull(name);
        return this;
    }

    @Override
    public SpongeSimpleCustomDataBuilder id(String id) {
        checkNotNull(id);
        checkArgument(StringUtils.countMatches(id, ":") == 1, "Key ID did not have a plugin!");
        this.id = id;
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public SpongeSimpleCustomDataBuilder from(SimpleCustomData value) {
        this.version = value.getContentVersion();
        this.updaters = ImmutableSet.copyOf(value.getContentUpdaters());
        //TODO
        return this;
    }

    @Override
    public <T> SimpleCustomData<T> build() {
        //TDO CHECK
        return new SpongeSimpleCustomData<>(this);
    }

    @Override
    public SpongeSimpleCustomDataBuilder reset() {
        version = 1;
        updaters = ImmutableSet.of();
        name = null;
        id = null;
        return this;
    }
}
