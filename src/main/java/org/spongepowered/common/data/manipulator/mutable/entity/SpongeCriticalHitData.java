package org.spongepowered.common.data.manipulator.mutable.entity;

import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableCriticalHitData;
import org.spongepowered.api.data.manipulator.mutable.entity.CriticalHitData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeCriticalHitData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.common.data.value.SpongeValueFactory;

public class SpongeCriticalHitData extends AbstractSingleData<Boolean, CriticalHitData, ImmutableCriticalHitData> implements CriticalHitData {

    public SpongeCriticalHitData(boolean value) {
        super(CriticalHitData.class, value, Keys.CRITICAL_HIT);
    }

    public SpongeCriticalHitData() {
        this(false);
    }

    @Override
    public CriticalHitData copy() {
        return new SpongeCriticalHitData(getValue());
    }

    @Override
    public ImmutableCriticalHitData asImmutable() {
        return new ImmutableSpongeCriticalHitData(getValue());
    }

    @Override
    public int compareTo(CriticalHitData o) {
        return ComparisonChain.start()
                .compare(getValue(), o.criticalHit().get())
                .result();
    }

    @Override
    public Value<Boolean> criticalHit() {
        return SpongeValueFactory.getInstance().createValue(Keys.CRITICAL_HIT, getValue(), false);
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(Keys.CRITICAL_HIT, getValue());
    }

    @Override
    protected Value<?> getValueGetter() {
        return criticalHit();
    }
}
