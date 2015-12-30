package org.spongepowered.common.data.manipulator.immutable.common;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.common.data.util.ComparatorUtil;

import java.util.Comparator;
import java.util.Optional;

public abstract class AbstractImmutableIntData<I extends ImmutableDataManipulator<I, M>, M extends DataManipulator<M, I>>
    extends AbstractImmutableBoundedComparableData<Integer, I, M> {

    public AbstractImmutableIntData(Class<I> immutableClass, int value,
        Key<? extends BaseValue<Integer>> usedKey,
        Class<? extends M> mutableClass, int lowerBound, int upperBound, int defaultValue) {
        super(immutableClass, value, usedKey, ComparatorUtil.intComparator(), mutableClass, lowerBound, upperBound, defaultValue);
    }

    @Override
    public Optional<I> with(BaseValue<?> value) {
        return null;
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
            .set(this.usedKey, this.value);
    }
}
