package org.spongepowered.common.data.manipulator;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.common.data.copy.CopyHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("unchecked")
abstract class SpongeDataManipulator implements DataManipulator {

    protected final Map<Key<?>, Object> values;

    SpongeDataManipulator(Map<Key<?>, Object> values) {
        this.values = values;
    }

    Map<Key<?>, Object> copyMap() {
        final Map<Key<?>, Object> copy = new HashMap<>();
        for (final Map.Entry<Key<?>, Object> entry : this.values.entrySet()) {
            copy.put(entry.getKey(), CopyHelper.copy(entry.getValue()));
        }
        return copy;
    }

    @Override
    public <E> Optional<E> get(Key<? extends Value<E>> key) {
        checkNotNull(key, "key");
        return Optional.ofNullable((E) CopyHelper.copy(this.values.get(key)));
    }

    @Override
    public <E, V extends Value<E>> Optional<V> getValue(Key<V> key) {
        checkNotNull(key, "key");
        final E element = (E) CopyHelper.copy(this.values.get(key));
        return element == null ? Optional.empty() : Optional.of(Value.genericMutableOf(key, element));
    }

    @Override
    public boolean supports(Key<?> key) {
        return true;
    }

    @Override
    public Set<Value.Immutable<?>> getValues() {
        return this.values.entrySet().stream()
                .map(entry -> Value.genericImmutableOf((Key) entry.getKey(), CopyHelper.copy(entry.getValue())).asImmutable())
                .collect(ImmutableSet.toImmutableSet());
    }
    
    @Override
    public String toString() {
        final MoreObjects.ToStringHelper builder = MoreObjects.toStringHelper(this);
        for (final Map.Entry<Key<?>, Object> entry : this.values.entrySet()) {
            builder.add(entry.getKey().getKey().toString(), entry.getValue());
        }
        return builder.toString();
    }
}
