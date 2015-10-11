package org.spongepowered.common.data.processor.value.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;

public class PlayingValueProcessor extends AbstractSpongeValueProcessor<EntityVillager, Boolean, Value<Boolean>> {

    public PlayingValueProcessor() {
        super(EntityVillager.class, Keys.IS_PLAYING);
    }

    @Override
    protected Value<Boolean> constructValue(Boolean value) {
        return new SpongeValue<>(getKey(), false, value);
    }

    @Override
    protected boolean set(EntityVillager container, Boolean value) {
        container.setPlaying(value);
        return true;
    }

    @Override
    protected Optional<Boolean> getVal(EntityVillager container) {
        return Optional.of(container.isPlaying());
    }

    @Override
    protected ImmutableValue<Boolean> constructImmutableValue(Boolean value) {
        return ImmutableSpongeValue.cachedOf(getKey(), false, value);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionBuilder.failNoData();
    }
}
