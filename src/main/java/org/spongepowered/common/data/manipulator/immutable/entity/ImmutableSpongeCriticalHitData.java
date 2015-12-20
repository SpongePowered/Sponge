package org.spongepowered.common.data.manipulator.immutable.entity;

import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableCriticalHitData;
import org.spongepowered.api.data.manipulator.mutable.entity.CriticalHitData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeCriticalHitData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

public class ImmutableSpongeCriticalHitData extends AbstractImmutableSingleData<Boolean, ImmutableCriticalHitData, CriticalHitData>
        implements ImmutableCriticalHitData {

    public ImmutableSpongeCriticalHitData(boolean value) {
        super(ImmutableCriticalHitData.class, value, Keys.CRITICAL_HIT);
    }

    @Override
    public CriticalHitData asMutable() {
        return new SpongeCriticalHitData(getValue());
    }

    @Override
    public ImmutableValue<Boolean> criticalHit() {
        return ImmutableSpongeValue.cachedOf(Keys.CRITICAL_HIT, false, getValue());
    }

    @Override
    public int compareTo(ImmutableCriticalHitData o) {
        return ComparisonChain.start()
                .compare(getValue(), o.criticalHit().get())
                .result();
    }

    @Override
    protected ImmutableValue<?> getValueGetter() {
        return criticalHit();
    }
}
