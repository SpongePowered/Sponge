package org.spongepowered.common.data.manipulator;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.MergeFunction;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ImmutableDataManipulatorFactory implements DataManipulator.Immutable.Factory {

    @Override
    public DataManipulator.Immutable of() {
        return new ImmutableDataManipulator(ImmutableMap.of());
    }

    @Override
    public DataManipulator.Immutable of(Iterable<? extends Value<?>> values) {
        final Map<Key<?>, Object> mappedValues = MutableDataManipulatorFactory.mapValues(values);
        return new ImmutableDataManipulator(Collections.unmodifiableMap(mappedValues));
    }

    @Override
    public DataManipulator.Immutable of(ValueContainer valueContainer) {
        checkNotNull(valueContainer, "valueContainer");
        if (valueContainer instanceof DataManipulator.Immutable) {
            return (DataManipulator.Immutable) valueContainer;
        }
        final Map<Key<?>, Object> values = new HashMap<>();
        MutableDataManipulator.copyFrom(values, valueContainer, MergeFunction.REPLACEMENT_PREFERRED);
        return new ImmutableDataManipulator(Collections.unmodifiableMap(values));
    }
}
