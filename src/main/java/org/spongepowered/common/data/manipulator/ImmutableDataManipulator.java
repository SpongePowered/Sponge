package org.spongepowered.common.data.manipulator;

import static com.google.common.base.Preconditions.checkNotNull;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;

import java.util.Map;
import java.util.Set;

final class ImmutableDataManipulator extends SpongeDataManipulator implements DataManipulator.Immutable {

    @Nullable private Set<Value.Immutable<?>> cachedValues;

    ImmutableDataManipulator(Map<Key<?>, Object> values) {
        super(values);
    }

    @Override
    public Mutable asMutableCopy() {
        return new MutableDataManipulator(this.copyMap());
    }

    @Override
    public Immutable without(Key<?> key) {
        checkNotNull(key, "key");
        if (!this.values.containsKey(key)) {
            return this;
        }
        return DataManipulator.Immutable.super.without(key);
    }

    @Override
    public Mutable asMutable() {
        return this.asMutableCopy();
    }

    @Override
    public Set<Key<?>> getKeys() {
        return this.values.keySet();
    }

    @Override
    public Set<Value.Immutable<?>> getValues() {
        if (this.cachedValues == null) {
            this.cachedValues = super.getValues();
        }
        return this.cachedValues;
    }
}
