/*
 * Copyright (c) 2015-2016 VoxelBox <http://engine.thevoxelbox.com>.
 * All Rights Reserved.
 */
package org.spongepowered.common.data.persistence;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;

import java.util.Optional;

public class NonCloningDataContainer extends NonCloningDataView implements DataContainer {

    @Override
    public Optional<DataView> getParent() {
        return Optional.empty();
    }

    @Override
    public final DataContainer getContainer() {
        return this;
    }

    @Override
    public DataContainer set(DataQuery path, Object value) {
        return (DataContainer) super.set(path, value);
    }

    @Override
    public <E> DataContainer set(Key<? extends BaseValue<E>> key, E value) {
        return set(checkNotNull(key).getQuery(), value);
    }

    @Override
    public DataContainer remove(DataQuery path) {
        return (DataContainer) super.remove(path);
    }
}
