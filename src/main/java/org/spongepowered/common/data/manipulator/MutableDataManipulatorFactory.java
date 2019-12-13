package org.spongepowered.common.data.manipulator;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.common.data.copy.CopyHelper;

import java.util.HashMap;
import java.util.Map;

public final class MutableDataManipulatorFactory implements DataManipulator.Mutable.Factory {

    @Override
    public DataManipulator.Mutable of() {
        return new MutableDataManipulator();
    }

    @Override
    public DataManipulator.Mutable of(Iterable<? extends Value<?>> values) {
        return new MutableDataManipulator(mapValues(values));
    }

    static Map<Key<?>, Object> mapValues(Iterable<? extends Value<?>> values) {
        checkNotNull(values, "values");
        final Map<Key<?>, Object> mappedValues = new HashMap<>();
        for (final Value<?> value : values) {
            mappedValues.put(value.getKey(), CopyHelper.copy(value.get()));
        }
        return mappedValues;
    }

    @Override
    public DataManipulator.Mutable of(ValueContainer valueContainer) {
        checkNotNull(valueContainer, "valueContainer");
        final MutableDataManipulator manipulator = new MutableDataManipulator();
        manipulator.copyFrom(valueContainer);
        return manipulator;
    }
}
