package org.spongepowered.common.data.manipulator.mutable.entity;

import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableLeashData;
import org.spongepowered.api.data.manipulator.mutable.entity.LeashData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeLeashData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.common.data.value.SpongeValueFactory;

public class SpongeLeashData extends AbstractSingleData<Entity, LeashData, ImmutableLeashData> implements LeashData {

    public SpongeLeashData(Entity value) {
        super(LeashData.class, value, Keys.LEASH_HOLDER);
    }

    @Override
    public LeashData copy() {
        return new SpongeLeashData(getValue());
    }

    @Override
    public ImmutableLeashData asImmutable() {
        return new ImmutableSpongeLeashData(getValue());
    }

    @Override
    public int compareTo(LeashData o) {
        return ComparisonChain.start()
                .compare(getValue().getType().getId(), o.leashHolder().get().getType().getId())
                .result();
    }

    @Override
    public Value<Entity> leashHolder() {
        return SpongeValueFactory.getInstance().createValue(Keys.LEASH_HOLDER, getValue());
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(Keys.LEASH_HOLDER, getValue());
    }

    @Override
    protected Value<?> getValueGetter() {
        return leashHolder();
    }
}
