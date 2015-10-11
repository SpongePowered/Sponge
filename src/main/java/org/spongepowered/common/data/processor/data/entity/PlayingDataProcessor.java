package org.spongepowered.common.data.processor.data.entity;


import net.minecraft.entity.passive.EntityVillager;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutablePlayingData;
import org.spongepowered.api.data.manipulator.mutable.entity.PlayingData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongePlayingData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

import java.util.Optional;

public class PlayingDataProcessor extends AbstractEntitySingleDataProcessor<EntityVillager, Boolean, Value<Boolean>, PlayingData, ImmutablePlayingData> {

    public PlayingDataProcessor() {
        super(EntityVillager.class, Keys.IS_PLAYING);
    }

    @Override
    protected boolean set(EntityVillager entity, Boolean value) {
        entity.setPlaying(value);
        return true;
    }

    @Override
    protected Optional<Boolean> getVal(EntityVillager entity) {
        return Optional.of(entity.isPlaying());
    }

    @Override
    protected ImmutableValue<Boolean> constructImmutableValue(Boolean value) {
        return ImmutableSpongeValue.cachedOf(key, false, value);
    }

    @Override
    protected PlayingData createManipulator() {
        return new SpongePlayingData();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionBuilder.failNoData();
    }
}
