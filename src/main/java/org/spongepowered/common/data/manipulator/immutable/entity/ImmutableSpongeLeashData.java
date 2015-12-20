package org.spongepowered.common.data.manipulator.immutable.entity;

import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableLeashData;
import org.spongepowered.api.data.manipulator.mutable.entity.LeashData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeLeashData;
import org.spongepowered.common.data.value.immutable.common.ImmutableSpongeEntityValue;

public class ImmutableSpongeLeashData extends AbstractImmutableSingleData<Entity, ImmutableLeashData, LeashData> implements ImmutableLeashData {

    public ImmutableSpongeLeashData(Entity value) {
        super(ImmutableLeashData.class, value, Keys.LEASH_HOLDER);
    }

    @Override
    public LeashData asMutable() {
        return new SpongeLeashData(getValue());
    }

    @Override
    public ImmutableValue<Entity> leashHolder() {
        return new ImmutableSpongeEntityValue<>(Keys.LEASH_HOLDER, getValue());
    }

    @Override
    public int compareTo(ImmutableLeashData o) {
        return ComparisonChain.start()
                .compare(getValue().getType().getId(), o.leashHolder().get().getType().getId())
                .result();
    }

    @Override
    protected ImmutableValue<?> getValueGetter() {
        return leashHolder();
    }
}
